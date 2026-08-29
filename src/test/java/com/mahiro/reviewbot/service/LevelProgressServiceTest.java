package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.model.CodeReview;
import com.mahiro.reviewbot.model.Problem;
import com.mahiro.reviewbot.repository.ProblemRepository;
import com.mahiro.reviewbot.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LevelProgressServiceTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private LevelProgressService service;

    @BeforeEach
    void setUp() {
        service = new LevelProgressService(problemRepository, reviewRepository);
    }

    private Problem problem(long id, int levelId) {
        Problem p = new Problem();
        p.setId(id);
        p.setLevelId(levelId);
        return p;
    }

    private CodeReview review(long id, Long problemId, Boolean correct) {
        CodeReview r = new CodeReview();
        r.setId(id);
        r.setProblemId(problemId);
        r.setIsCorrect(correct);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    void isCleared_requiresAtLeastTwoCorrectProblemsInLevel() {
        when(problemRepository.findAll()).thenReturn(List.of(problem(1, 10), problem(2, 10)));
        // reviewsは新しい順(id DESC)で渡す必要がある(実装はfindAllOrderByCreatedAtDescの契約に依存)
        when(reviewRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(
                review(2, 2L, true),
                review(1, 1L, true)
        ));

        Map<Integer, Integer> counts = service.correctCountByLevel();

        assertThat(counts.get(10)).isEqualTo(2);
        assertThat(service.isCleared(10, counts)).isTrue();
    }

    @Test
    void isCleared_isFalseWithOnlyOneCorrectProblem() {
        when(problemRepository.findAll()).thenReturn(List.of(problem(1, 10)));
        when(reviewRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(review(1, 1L, true)));

        Map<Integer, Integer> counts = service.correctCountByLevel();

        assertThat(counts.getOrDefault(10, 0)).isEqualTo(1);
        assertThat(service.isCleared(10, counts)).isFalse();
    }

    @Test
    void correctCountByLevel_onlyCountsTheLatestReviewPerProblem() {
        when(problemRepository.findAll()).thenReturn(List.of(problem(1, 10)));
        // 同じ問題への再提出: 古い方は不正解、新しい方(id大)が正解 -> 最新判定の正解として数える
        when(reviewRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(
                review(5, 1L, true),
                review(3, 1L, false)
        ));

        Map<Integer, Integer> counts = service.correctCountByLevel();

        assertThat(counts.get(10)).isEqualTo(1);
    }

    @Test
    void correctCountByLevel_ignoresReviewsWithoutProblemIdOrJudgement() {
        when(problemRepository.findAll()).thenReturn(List.of(problem(1, 10)));
        when(reviewRepository.findAllOrderByCreatedAtDesc()).thenReturn(List.of(
                review(1, null, true),   // アドホックレビュー(問題紐付けなし)
                review(2, 1L, null)      // 正誤未判定
        ));

        Map<Integer, Integer> counts = service.correctCountByLevel();

        assertThat(counts).isEmpty();
    }
}
