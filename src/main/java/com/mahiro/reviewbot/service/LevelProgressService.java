package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * レベルごとの「クリア済みかどうか」の判定を1箇所にまとめるService。
 * LevelController(一覧表示)とStatsService(Silver/Goldバッジ集計)の両方から使う。
 *
 * クリア条件: そのレベルの問題のうち、直近の提出が「正解」判定だったものが
 * {@link #REQUIRED_CORRECT_TO_CLEAR} 問以上ある。1問だけ正解しても「たまたま」の
 * 可能性があるため、資格レベルの習熟度としては複数問の正解を要求している。
 */
@Service
public class LevelProgressService {

    public static final int REQUIRED_CORRECT_TO_CLEAR = 2;

    private final ProblemRepository problemRepository;
    private final ReviewRepository reviewRepository;

    public LevelProgressService(ProblemRepository problemRepository, ReviewRepository reviewRepository) {
        this.problemRepository = problemRepository;
        this.reviewRepository = reviewRepository;
    }

    /** レベルIDごとに、直近の提出が「正解」判定だった問題の数を返す */
    public Map<Integer, Integer> correctCountByLevel() {
        Map<Long, Boolean> latestCorrectByProblemId = new HashMap<>();
        // reviews は新しい順(id DESC)なので、まだ記録されていないproblemIdだけ採用すれば「最新の判定」になる
        for (CodeReview review : reviewRepository.findAllOrderByCreatedAtDesc()) {
            if (review.getProblemId() == null || review.getIsCorrect() == null) {
                continue;
            }
            latestCorrectByProblemId.putIfAbsent(review.getProblemId(), review.getIsCorrect());
        }

        Map<Integer, Integer> counts = new HashMap<>();
        for (Problem problem : problemRepository.findAll()) {
            if (Boolean.TRUE.equals(latestCorrectByProblemId.get(problem.getId()))) {
                counts.merge(problem.getLevelId(), 1, Integer::sum);
            }
        }
        return counts;
    }

    public boolean isCleared(int levelId, Map<Integer, Integer> correctCounts) {
        return correctCounts.getOrDefault(levelId, 0) >= REQUIRED_CORRECT_TO_CLEAR;
    }
}
