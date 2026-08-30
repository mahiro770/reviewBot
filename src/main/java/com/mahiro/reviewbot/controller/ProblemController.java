package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.FavoriteRequest;
import com.mahiro.reviewbot.dto.GenerateProblemRequest;
import com.mahiro.reviewbot.dto.PageResponse;
import com.mahiro.reviewbot.dto.ProblemResponse;
import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.model.Level;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.GoalRepository;
import com.mahiro.reviewbot.repository.LevelRepository;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import com.mahiro.reviewbot.service.GeminiProblemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller層: 問題集(レベル別に生成するプログラミング問題)を扱う。
 */
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_GENERATE_COUNT = 5;
    private static final int MAX_GENERATE_COUNT = 10;

    private final ProblemRepository problemRepository;
    private final GoalRepository goalRepository;
    private final ReviewRepository reviewRepository;
    private final LevelRepository levelRepository;
    private final GeminiProblemService geminiProblemService;

    public ProblemController(ProblemRepository problemRepository, GoalRepository goalRepository,
                              ReviewRepository reviewRepository, LevelRepository levelRepository,
                              GeminiProblemService geminiProblemService) {
        this.problemRepository = problemRepository;
        this.goalRepository = goalRepository;
        this.reviewRepository = reviewRepository;
        this.levelRepository = levelRepository;
        this.geminiProblemService = geminiProblemService;
    }

    /** 指定レベルの生成済み問題一覧(新しい順、ページング対応) */
    @GetMapping
    public ResponseEntity<?> listProblems(@RequestParam int levelId,
                                           @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit,
                                           @RequestParam(defaultValue = "0") int offset) {
        if (levelRepository.findById(levelId).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "不正なレベルIDです。"));
        }
        limit = clampLimit(limit);

        List<Problem> page = problemRepository.findByLevelId(levelId, limit, offset);
        int total = problemRepository.countByLevelId(levelId);
        List<ProblemResponse> items = toResponses(page);

        return ResponseEntity.ok(PageResponse.of(items, total, limit, offset));
    }

    /** 指定レベルの新しい問題をGeminiにまとめて生成してもらい保存する(デフォルト5問、最大10問) */
    @PostMapping("/generate")
    public ResponseEntity<?> generateProblems(@RequestBody GenerateProblemRequest request) {
        try {
            Level level = levelRepository.findById(request.getLevelId())
                    .orElseThrow(() -> new IllegalArgumentException("不正なレベルIDです。"));
            int count = request.getCount() <= 0 ? DEFAULT_GENERATE_COUNT : Math.min(request.getCount(), MAX_GENERATE_COUNT);

            Goal goal = goalRepository.findLatest().orElse(null);
            List<String> recentTitles = problemRepository.findRecentTitlesByLevel(level.id(), 10);

            List<GeminiProblemService.ProblemResult> results =
                    geminiProblemService.generateProblems(level, goal, recentTitles, count);

            List<ProblemResponse> saved = results.stream().map(result -> {
                Problem problem = new Problem();
                problem.setLevelId(level.id());
                problem.setTitle(result.title());
                problem.setDifficulty(result.difficulty());
                problem.setDescription(result.description());
                problem.setCreatedAt(LocalDateTime.now());
                Problem p = problemRepository.save(problem);
                return ProblemResponse.from(p, level.title(), false, null);
            }).toList();

            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "問題の生成中にエラーが発生しました: " + e.getMessage()));
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

    /** お気に入り一覧(ページング対応) */
    @GetMapping("/favorites")
    public PageResponse<ProblemResponse> listFavorites(@RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit,
                                                         @RequestParam(defaultValue = "0") int offset) {
        limit = clampLimit(limit);
        List<ProblemResponse> all = toResponses(problemRepository.findFavorites());
        return paginate(all, limit, offset);
    }

    /** 直近の提出が「不正解」判定だった問題の一覧(ページング対応) */
    @GetMapping("/mistakes")
    public PageResponse<ProblemResponse> listMistakes(@RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit,
                                                        @RequestParam(defaultValue = "0") int offset) {
        limit = clampLimit(limit);
        List<ProblemResponse> all = toResponses(problemRepository.findAll()).stream()
                .filter(res -> Boolean.FALSE.equals(res.getCorrect()))
                .toList();
        return paginate(all, limit, offset);
    }

    private int clampLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * favorites/mistakesは正誤判定の絡みで一度全件をJava側で計算する必要があるため、
     * DBのLIMIT/OFFSETではなく計算済みリストをここでページングする
     * (個人利用規模のデータ量を前提としたシンプルな実装)。
     */
    private PageResponse<ProblemResponse> paginate(List<ProblemResponse> all, int limit, int offset) {
        int from = Math.min(offset, all.size());
        int to = Math.min(offset + limit, all.size());
        return PageResponse.of(all.subList(from, to), all.size(), limit, offset);
    }

    /** 複数の問題を、そのレビュー(正誤・提出済みか)込みでまとめてDTOに変換する(N+1回避) */
    private List<ProblemResponse> toResponses(List<Problem> problems) {
        List<Long> ids = problems.stream().map(Problem::getId).toList();
        Map<Long, List<CodeReview>> attemptsByProblemId = reviewRepository.findByProblemIds(ids).stream()
                .collect(Collectors.groupingBy(CodeReview::getProblemId));
        Map<Integer, String> levelTitles = levelRepository.findAll().stream()
                .collect(Collectors.toMap(Level::id, Level::title));

        return problems.stream()
                .map(problem -> toResponse(problem, levelTitles.get(problem.getLevelId()),
                        attemptsByProblemId.getOrDefault(problem.getId(), List.of())))
                .toList();
    }

    private ProblemResponse toResponse(Problem problem) {
        String levelTitle = levelRepository.findById(problem.getLevelId()).map(Level::title).orElse("");
        return toResponse(problem, levelTitle, reviewRepository.findByProblemIdOrderByCreatedAtDesc(problem.getId()));
    }

    private ProblemResponse toResponse(Problem problem, String levelTitle, List<CodeReview> attempts) {
        boolean attempted = !attempts.isEmpty();
        Boolean correct = attempts.stream()
                .map(CodeReview::getIsCorrect)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return ProblemResponse.from(problem, levelTitle, attempted, correct);
    }
}
