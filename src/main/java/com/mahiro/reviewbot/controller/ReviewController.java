package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.PageResponse;
import com.mahiro.reviewbot.dto.ReviewRequest;
import com.mahiro.reviewbot.dto.ReviewResponse;
import com.mahiro.reviewbot.dto.ReviewSummary;
import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import com.mahiro.reviewbot.service.GeminiReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller層: HTTPリクエストを受け取り、Service/Repositoryに処理を委譲する。
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final GeminiReviewService geminiReviewService;
    private final ReviewRepository reviewRepository;
    private final ProblemRepository problemRepository;

    public ReviewController(GeminiReviewService geminiReviewService, ReviewRepository reviewRepository,
                             ProblemRepository problemRepository) {
        this.geminiReviewService = geminiReviewService;
        this.reviewRepository = reviewRepository;
        this.problemRepository = problemRepository;
    }

    /** コードを送ってレビューしてもらい、結果をDBに保存する。problemIdがあれば正誤判定も行う */
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            Problem problem = request.getProblemId() != null
                    ? problemRepository.findById(request.getProblemId()).orElse(null)
                    : null;

            GeminiReviewService.ReviewResult result = geminiReviewService.reviewCode(request.getCode(), problem);

            CodeReview review = new CodeReview();
            review.setCode(request.getCode());
            review.setReview(result.reviewText());
            review.setScore(result.score());
            review.setCreatedAt(LocalDateTime.now());
            review.setProblemId(request.getProblemId());
            review.setIsCorrect(result.correct());

            CodeReview saved = reviewRepository.save(review);
            return ResponseEntity.ok(ReviewResponse.from(saved));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "レビュー中にエラーが発生しました: " + e.getMessage()));
        }
    }

    /** 過去のレビュー履歴一覧(新しい順、ページング対応) */
    @GetMapping
    public PageResponse<ReviewSummary> listReviews(@RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit,
                                                     @RequestParam(defaultValue = "0") int offset) {
        int effectiveLimit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        List<CodeReview> page = reviewRepository.findPageOrderByCreatedAtDesc(effectiveLimit, offset);
        List<Long> problemIds = page.stream()
                .map(CodeReview::getProblemId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> titlesById = problemRepository.findTitlesByIds(problemIds);

        List<ReviewSummary> items = page.stream()
                .map(review -> ReviewSummary.from(review,
                        review.getProblemId() == null ? null : titlesById.get(review.getProblemId())))
                .toList();
        int total = reviewRepository.count();

        return PageResponse.of(items, total, effectiveLimit, offset);
    }

    /** 過去のレビュー1件の詳細 */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(@PathVariable long id) {
        return reviewRepository.findById(id)
                .map(review -> ResponseEntity.ok(Map.of(
                        "id", review.getId(),
                        "code", review.getCode(),
                        "review", review.getReview(),
                        "score", review.getScore() == null ? "" : review.getScore(),
                        "createdAt", review.getCreatedAt().toString(),
                        "problemId", review.getProblemId() == null ? "" : review.getProblemId(),
                        "isCorrect", review.getIsCorrect() == null ? "" : review.getIsCorrect()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
