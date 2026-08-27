package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.ProblemResponse;
import com.mahiro.reviewbot.model.DailyProblem;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.repository.GoalRepository;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import com.mahiro.reviewbot.service.ClaudeProblemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller層: 今日の問題の取得(なければClaudeで生成)と、過去の問題一覧を扱う。
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemRepository problemRepository;
    private final GoalRepository goalRepository;
    private final ReviewRepository reviewRepository;
    private final ClaudeProblemService claudeProblemService;

    public ProblemController(ProblemRepository problemRepository, GoalRepository goalRepository,
                              ReviewRepository reviewRepository, ClaudeProblemService claudeProblemService) {
        this.problemRepository = problemRepository;
        this.goalRepository = goalRepository;
        this.reviewRepository = reviewRepository;
        this.claudeProblemService = claudeProblemService;
    }

    /** 今日の問題を取得する。まだ生成されていなければClaudeに生成してもらい保存する */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayProblem() {
        try {
            LocalDate today = LocalDate.now();
            DailyProblem problem = problemRepository.findByDate(today)
                    .orElseGet(() -> generateAndSaveTodayProblem(today));

            boolean solved = reviewRepository.existsByProblemId(problem.getId());
            return ResponseEntity.ok(ProblemResponse.from(problem, solved));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "問題の生成中にエラーが発生しました: " + e.getMessage()));
        }
    }

    /** 過去に出題した問題の一覧(新しい順) */
    @GetMapping
    public List<ProblemResponse> listProblems() {
        return problemRepository.findAllOrderByDateDesc().stream()
                .map(problem -> ProblemResponse.from(problem, reviewRepository.existsByProblemId(problem.getId())))
                .toList();
    }

    private DailyProblem generateAndSaveTodayProblem(LocalDate today) {
        Goal goal = goalRepository.findLatest().orElse(null);
        List<String> recentTitles = problemRepository.findRecentTitles(10);

        ClaudeProblemService.ProblemResult result = claudeProblemService.generateProblem(goal, recentTitles);

        DailyProblem problem = new DailyProblem();
        problem.setProblemDate(today);
        problem.setTitle(result.title());
        problem.setDifficulty(result.difficulty());
        problem.setDescription(result.description());
        problem.setCreatedAt(LocalDateTime.now());

        return problemRepository.save(problem);
    }
}
