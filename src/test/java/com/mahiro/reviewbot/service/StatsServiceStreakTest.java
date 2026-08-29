package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.repository.GoalRepository;
import com.mahiro.reviewbot.repository.LevelRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** computeStreak はDB不要の純粋なロジックなので、Mockitoでインスタンスだけ作って直接テストする */
@ExtendWith(MockitoExtension.class)
class StatsServiceStreakTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private LevelRepository levelRepository;
    @Mock
    private LevelProgressService levelProgressService;

    private StatsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(reviewRepository, goalRepository, levelRepository, levelProgressService);
    }

    private CodeReview reviewOn(LocalDate date) {
        CodeReview review = new CodeReview();
        review.setCreatedAt(date.atTime(12, 0));
        return review;
    }

    @Test
    void computeStreak_isZeroWithNoReviews() {
        assertThat(statsService.computeStreak(List.of())).isZero();
    }

    @Test
    void computeStreak_countsConsecutiveDaysEndingToday() {
        LocalDate today = LocalDate.now();
        List<CodeReview> reviews = List.of(
                reviewOn(today),
                reviewOn(today.minusDays(1)),
                reviewOn(today.minusDays(2))
        );

        assertThat(statsService.computeStreak(reviews)).isEqualTo(3);
    }

    @Test
    void computeStreak_stillCountsWhenTodayHasNoReviewYetButYesterdayDid() {
        LocalDate today = LocalDate.now();
        List<CodeReview> reviews = List.of(reviewOn(today.minusDays(1)));

        assertThat(statsService.computeStreak(reviews)).isEqualTo(1);
    }

    @Test
    void computeStreak_stopsAtGapInDates() {
        LocalDate today = LocalDate.now();
        List<CodeReview> reviews = List.of(
                reviewOn(today),
                // 昨日が抜けている
                reviewOn(today.minusDays(2)),
                reviewOn(today.minusDays(3))
        );

        assertThat(statsService.computeStreak(reviews)).isEqualTo(1);
    }

    @Test
    void computeStreak_isZeroWhenLastReviewWasTwoOrMoreDaysAgo() {
        LocalDate today = LocalDate.now();
        List<CodeReview> reviews = List.of(reviewOn(today.minusDays(2)));

        assertThat(statsService.computeStreak(reviews)).isZero();
    }
}
