package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.FavoriteRequest;
import com.mahiro.reviewbot.dto.GenerateProblemRequest;
import com.mahiro.reviewbot.dto.ProblemResponse;
import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.model.Level;
import com.mahiro.reviewbot.model.LevelCatalog;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.GoalRepository;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import com.mahiro.reviewbot.service.GeminiProblemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller層: 問題集(レベル別に生成するプログラミング問題)を扱う。
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepository;
    private final GoalRepository goalRepository;
    private final ReviewRepository reviewRepository;
    private final GeminiProblemService geminiProblemService;

    public ProblemController(ProblemRepository problemRepository, GoalRepository goalRepository,
                              ReviewRepository reviewRepository, GeminiProblemService geminiProblemService) {
        this.problemRepository = problemRepository;
        this.goalRepository = goalRepository;
        this.reviewRepository = reviewRepository;
        this.geminiProblemService = geminiProblemService;
    }

    /** 指定レベルの生成済み問題一覧(新しい順) */
    @GetMapping
    public ResponseEntity<?> listProblems(@RequestParam int levelId) {
        if (LevelCatalog.findById(levelId).isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "不正なレベルIDです。"));
        }
        List<ProblemResponse> problems = problemRepository.findByLevelId(levelId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(problems);
    }

    /** 指定レベルの新しい問題をGeminiに生成してもらい保存する */
    @PostMapping("/generate")
    public ResponseEntity<?> generateProblem(@RequestBody GenerateProblemRequest request) {
        try {
            Level level = LevelCatalog.findById(request.getLevelId())
                    .orElseThrow(() -> new IllegalArgumentException("不正なレベルIDです。"));

            Goal goal = goalRepository.findLatest().orElse(null);
            List<String> recentTitles = problemRepository.findRecentTitlesByLevel(level.id(), 10);

            GeminiProblemService.ProblemResult result = geminiProblemService.generateProblem(level, goal, recentTitles);

            Problem problem = new Problem();
            problem.setLevelId(level.id());
            problem.setTitle(result.title());
            problem.setDifficulty(result.difficulty());
            problem.setDescription(result.description());
            problem.setCreatedAt(LocalDateTime.now());

            Problem saved = problemRepository.save(problem);
            return ResponseEntity.ok(toResponse(saved));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "問題の生成中にエラーが発生しました: " + e.getMessage()));
        }
    }

    /** お気に入り登録/解除 */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> setFavorite(@PathVariable long id, @RequestBody FavoriteRequest request) {
        Optional<Problem> problem = problemRepository.findById(id);
        if (problem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        problemRepository.setFavorite(id, request.isFavorite());
        return ResponseEntity.ok(toResponse(problemRepository.findById(id).orElseThrow()));
    }

    /** お気に入り一覧 */
    @GetMapping("/favorites")
    public List<ProblemResponse> listFavorites() {
        return problemRepository.findFavorites().stream()
                .map(this::toResponse)
                .toList();
    }

    /** 直近の提出が「不正解」判定だった問題の一覧 */
    @GetMapping("/mistakes")
    public List<ProblemResponse> listMistakes() {
        return problemRepository.findAll().stream()
                .map(problem -> toResponse(problem))
                .filter(res -> Boolean.FALSE.equals(res.getCorrect()))
                .toList();
    }

    private ProblemResponse toResponse(Problem problem) {
        List<CodeReview> attempts = reviewRepository.findByProblemIdOrderByCreatedAtDesc(problem.getId());
        boolean attempted = !attempts.isEmpty();
        Boolean correct = attempts.stream()
                .map(CodeReview::getIsCorrect)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        return ProblemResponse.from(problem, attempted, correct);
    }
}
