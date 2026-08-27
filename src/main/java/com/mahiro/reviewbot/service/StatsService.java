package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.dto.StatsResponse;
import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.repository.GoalRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * レビュー履歴と目標から、進捗タブに表示する統計を集計するService。
 * (単純さを優先し、複雑なSQL集約は使わず全件Java側でロードして集計する)
 */
@Service
public class StatsService {

    private static final int ACTIVITY_WINDOW_DAYS = 30;

    private final ReviewRepository reviewRepository;
    private final GoalRepository goalRepository;

    public StatsService(ReviewRepository reviewRepository, GoalRepository goalRepository) {
        this.reviewRepository = reviewRepository;
        this.goalRepository = goalRepository;
    }

    public StatsResponse getStats() {
        List<CodeReview> reviews = reviewRepository.findAllOrderByCreatedAtDesc();

        StatsResponse stats = new StatsResponse();
        stats.setTotalReviews(reviews.size());
        stats.setScoreTrend(buildScoreTrend(reviews));
        stats.setDailyActivity(buildDailyActivity(reviews));
        stats.setStreakDays(computeStreak(reviews));

        List<Integer> scores = reviews.stream()
                .map(CodeReview::getScore)
                .filter(Objects::nonNull)
                .toList();
        stats.setAverageScore(scores.isEmpty() ? null : scores.stream().mapToInt(Integer::intValue).average().orElse(0));

        goalRepository.findLatest().ifPresent(goal -> applyGoalProgress(stats, goal));

        return stats;
    }

    private List<StatsResponse.ScorePoint> buildScoreTrend(List<CodeReview> reviews) {
        Map<LocalDate, List<Integer>> byDate = new TreeMap<>();
        for (CodeReview review : reviews) {
            if (review.getScore() == null) {
                continue;
            }
            LocalDate date = review.getCreatedAt().toLocalDate();
            byDate.computeIfAbsent(date, d -> new ArrayList<>()).add(review.getScore());
        }

        List<StatsResponse.ScorePoint> points = new ArrayList<>();
        byDate.forEach((date, scores) -> {
            double avg = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
            points.add(new StatsResponse.ScorePoint(date.toString(), avg));
        });
        return points;
    }

    private List<StatsResponse.ActivityPoint> buildDailyActivity(List<CodeReview> reviews) {
        Map<LocalDate, Integer> counts = new HashMap<>();
        for (CodeReview review : reviews) {
            LocalDate date = review.getCreatedAt().toLocalDate();
            counts.merge(date, 1, Integer::sum);
        }

        List<StatsResponse.ActivityPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = ACTIVITY_WINDOW_DAYS - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            points.add(new StatsResponse.ActivityPoint(date.toString(), counts.getOrDefault(date, 0)));
        }
        return points;
    }

    private int computeStreak(List<CodeReview> reviews) {
        Set<LocalDate> activeDates = new HashSet<>();
        for (CodeReview review : reviews) {
            activeDates.add(review.getCreatedAt().toLocalDate());
        }

        LocalDate today = LocalDate.now();
        LocalDate cursor = activeDates.contains(today) ? today : today.minusDays(1);

        int streak = 0;
        while (activeDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private void applyGoalProgress(StatsResponse stats, Goal goal) {
        if (goal.getTargetDate() == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        stats.setDaysRemaining(ChronoUnit.DAYS.between(today, goal.getTargetDate()));

        long totalDays = ChronoUnit.DAYS.between(goal.getStartDate(), goal.getTargetDate());
        long elapsedDays = ChronoUnit.DAYS.between(goal.getStartDate(), today);
        if (totalDays > 0) {
            int percent = (int) Math.round(elapsedDays * 100.0 / totalDays);
            stats.setProgressPercent(Math.max(0, Math.min(100, percent)));
        }
    }
}
