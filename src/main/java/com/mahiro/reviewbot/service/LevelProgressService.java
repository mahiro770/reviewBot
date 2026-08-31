package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    /** 次の問題生成時に難易度を調整する判断材料として使う、そのレベルでの直近の正誤傾向 */
    public record RecentPerformance(int correctCount, int totalCount) {
        private static final int MIN_SAMPLE_TO_ADJUST = 2;

        public boolean hasEnoughData() {
            return totalCount >= MIN_SAMPLE_TO_ADJUST;
        }

        public double correctRate() {
            return totalCount == 0 ? 0.0 : (double) correctCount / totalCount;
        }
    }

    private static final int RECENT_SAMPLE_SIZE = 5;

    /**
     * そのレベルの問題への提出のうち、直近(最大{@link #RECENT_SAMPLE_SIZE}件)の正誤判定を集計する。
     * 同じ問題への再提出も1件として数える(「今のスキル」に近い直近の傾向を見たいため)。
     */
    public RecentPerformance recentPerformance(int levelId) {
        List<Long> problemIds = problemRepository.findByLevelId(levelId).stream()
                .map(Problem::getId)
                .toList();
        if (problemIds.isEmpty()) {
            return new RecentPerformance(0, 0);
        }

        List<Boolean> recentJudgements = reviewRepository.findByProblemIds(problemIds).stream()
                .map(CodeReview::getIsCorrect)
                .filter(Objects::nonNull)
                .limit(RECENT_SAMPLE_SIZE)
                .toList();

        int correct = (int) recentJudgements.stream().filter(Boolean::booleanValue).count();
        return new RecentPerformance(correct, recentJudgements.size());
    }
}
