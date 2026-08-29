package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.CodeReview;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    private CodeReview newReview(String code, Integer score, Long problemId, Boolean correct) {
        CodeReview review = new CodeReview();
        review.setCode(code);
        review.setReview("dummy review for " + code);
        review.setScore(score);
        review.setCreatedAt(LocalDateTime.now());
        review.setProblemId(problemId);
        review.setIsCorrect(correct);
        return review;
    }

    @Test
    void save_and_findById_roundTripsAllFields() {
        CodeReview saved = reviewRepository.save(newReview("class A {}", 80, 5L, true));

        Optional<CodeReview> found = reviewRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("class A {}");
        assertThat(found.get().getScore()).isEqualTo(80);
        assertThat(found.get().getProblemId()).isEqualTo(5L);
        assertThat(found.get().getIsCorrect()).isTrue();
    }

    @Test
    void save_withNullScoreProblemIdAndCorrect_staysNull() {
        CodeReview saved = reviewRepository.save(newReview("class B {}", null, null, null));

        CodeReview found = reviewRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getScore()).isNull();
        assertThat(found.getProblemId()).isNull();
        assertThat(found.getIsCorrect()).isNull();
    }

    @Test
    void findByProblemIds_groupsReviewsByProblem_newestFirst() {
        CodeReview p1old = reviewRepository.save(newReview("p1-old", 40, 100L, false));
        CodeReview p1new = reviewRepository.save(newReview("p1-new", 90, 100L, true));
        CodeReview p2 = reviewRepository.save(newReview("p2", 70, 200L, true));
        reviewRepository.save(newReview("unrelated", 50, 999L, false));

        List<CodeReview> results = reviewRepository.findByProblemIds(List.of(100L, 200L));

        assertThat(results).extracting(CodeReview::getId)
                .containsExactlyInAnyOrder(p1old.getId(), p1new.getId(), p2.getId());
        // id DESC(新しい順)であること
        assertThat(results.get(0).getId()).isGreaterThan(results.get(results.size() - 1).getId());
    }

    @Test
    void findByProblemIds_withEmptyList_returnsEmpty() {
        assertThat(reviewRepository.findByProblemIds(List.of())).isEmpty();
    }

    @Test
    void existsByProblemId_reflectsWhetherAnyReviewIsLinked() {
        reviewRepository.save(newReview("linked", 60, 300L, null));

        assertThat(reviewRepository.existsByProblemId(300L)).isTrue();
        assertThat(reviewRepository.existsByProblemId(301L)).isFalse();
    }

    @Test
    void findPageOrderByCreatedAtDesc_respectsLimitAndOffsetAndTotalCount() {
        int before = reviewRepository.count();
        for (int i = 0; i < 5; i++) {
            reviewRepository.save(newReview("page-" + i, i, null, null));
        }

        List<CodeReview> firstPage = reviewRepository.findPageOrderByCreatedAtDesc(2, 0);
        List<CodeReview> secondPage = reviewRepository.findPageOrderByCreatedAtDesc(2, 2);

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(firstPage.get(0).getId()).isNotEqualTo(secondPage.get(0).getId());
        assertThat(reviewRepository.count()).isEqualTo(before + 5);
    }
}
