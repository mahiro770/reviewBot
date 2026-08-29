package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.LevelResponse;
import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Level;
import com.mahiro.reviewbot.model.LevelCatalog;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller層: 問題集のレベル一覧(Java Silver/Gold準拠のカリキュラム)を返す。
 */
@RestController
@RequestMapping("/api/levels")
public class LevelController {

    private final ProblemRepository problemRepository;
    private final ReviewRepository reviewRepository;

    public LevelController(ProblemRepository problemRepository, ReviewRepository reviewRepository) {
        this.problemRepository = problemRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public List<LevelResponse> listLevels() {
        Map<Long, Boolean> latestCorrectByProblemId = new HashMap<>();
        for (CodeReview review : reviewRepository.findAllOrderByCreatedAtDesc()) {
            if (review.getProblemId() == null || review.getIsCorrect() == null) {
                continue;
            }
            latestCorrectByProblemId.putIfAbsent(review.getProblemId(), review.getIsCorrect());
        }

        Map<Integer, Boolean> levelCleared = new HashMap<>();
        for (Problem problem : problemRepository.findAll()) {
            if (Boolean.TRUE.equals(latestCorrectByProblemId.get(problem.getId()))) {
                levelCleared.put(problem.getLevelId(), true);
            }
        }

        return LevelCatalog.LEVELS.stream()
                .map((Level level) -> LevelResponse.from(level, levelCleared.getOrDefault(level.id(), false)))
                .toList();
    }
}
