package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.ReviewRequest;
import com.mahiro.reviewbot.dto.ReviewResponse;
import com.mahiro.reviewbot.dto.ReviewSummary;
import com.mahiro.reviewbot.model.CodeReview;
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

    private final GeminiReviewService geminiReviewService;
    private final ReviewRepository reviewRepository;

    public ReviewController(GeminiReviewService geminiReviewService, ReviewRepository reviewRepository) {
        this.geminiReviewService = geminiReviewService;
        this.reviewRepository = reviewRepository;
    }

    /** コードを送ってレビューしてもらい、結果をDBに保存する */
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        try {
            GeminiReviewService.ReviewResult result = geminiReviewService.reviewCode(request.getCode());

            CodeReview review = new CodeReview();
            review.setCode(request.getCode());
            review.setReview(result.reviewText());
            review.setScore(result.score());
            review.setCreatedAt(LocalDateTime.now());
            review.setProblemId(request.getProblemId());

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

    /** 過去のレビュー履歴一覧(新しい順) */
    @GetMapping
    public List<ReviewSummary> listReviews() {
        return reviewRepository.findAllOrderByCreatedAtDesc().stream()
                .map(ReviewSummary::from)
                .toList();
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
                        "problemId", review.getProblemId() == null ? "" : review.getProblemId()
                )))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
