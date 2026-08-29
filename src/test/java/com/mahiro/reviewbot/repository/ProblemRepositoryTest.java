package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.Problem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProblemRepositoryTest {

    @Autowired
    private ProblemRepository problemRepository;

    private Problem newProblem(int levelId, String title) {
        Problem problem = new Problem();
        problem.setLevelId(levelId);
        problem.setTitle(title);
        problem.setDifficulty("初級");
        problem.setDescription("dummy description for " + title);
        problem.setCreatedAt(LocalDateTime.now());
        return problem;
    }

    @Test
    void save_startsAsNotFavoriteAndIsFindableById() {
        Problem saved = problemRepository.save(newProblem(1, "配列の合計を求める"));

        Optional<Problem> found = problemRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("配列の合計を求める");
        assertThat(found.get().getLevelId()).isEqualTo(1);
        assertThat(found.get().isFavorite()).isFalse();
    }

    @Test
    void setFavorite_togglesFlagInPlace() {
        Problem saved = problemRepository.save(newProblem(2, "文字列反転"));

        problemRepository.setFavorite(saved.getId(), true);
        assertThat(problemRepository.findById(saved.getId()).orElseThrow().isFavorite()).isTrue();

        problemRepository.setFavorite(saved.getId(), false);
        assertThat(problemRepository.findById(saved.getId()).orElseThrow().isFavorite()).isFalse();
    }

    @Test
    void findByLevelId_withPagination_returnsCorrectSliceAndCount() {
        int levelId = 3;
        int before = problemRepository.countByLevelId(levelId);
        for (int i = 0; i < 5; i++) {
            problemRepository.save(newProblem(levelId, "問題" + i));
        }

        List<Problem> firstPage = problemRepository.findByLevelId(levelId, 2, 0);
        List<Problem> secondPage = problemRepository.findByLevelId(levelId, 2, 2);

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(firstPage.get(0).getId()).isNotEqualTo(secondPage.get(0).getId());
        assertThat(problemRepository.countByLevelId(levelId)).isEqualTo(before + 5);
    }

    @Test
    void findRecentTitlesByLevel_returnsNewestTitlesFirstUpToLimit() {
        int levelId = 4;
        problemRepository.save(newProblem(levelId, "古い問題"));
        problemRepository.save(newProblem(levelId, "新しい問題"));

        List<String> titles = problemRepository.findRecentTitlesByLevel(levelId, 1);

        assertThat(titles).containsExactly("新しい問題");
    }

    @Test
    void findFavorites_onlyReturnsFavoritedProblems() {
        Problem favored = problemRepository.save(newProblem(5, "お気に入り問題"));
        problemRepository.save(newProblem(5, "普通の問題"));
        problemRepository.setFavorite(favored.getId(), true);

        List<Problem> favorites = problemRepository.findFavorites();

        assertThat(favorites).extracting(Problem::getId).contains(favored.getId());
        assertThat(favorites).allMatch(Problem::isFavorite);
    }
}
