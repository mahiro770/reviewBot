package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.LevelCatalog;
import com.mahiro.reviewbot.model.Problem;

import java.time.LocalDateTime;

/** 問題集の1問としてフロントエンドに返すデータ */
public class ProblemResponse {

    private Long id;
    private int levelId;
    private String levelTitle;
    private String title;
    private String difficulty;
    private String description;
    private boolean favorite;
    private boolean attempted;
    private Boolean correct;
    private LocalDateTime createdAt;

    public static ProblemResponse from(Problem problem, boolean attempted, Boolean correct) {
        ProblemResponse res = new ProblemResponse();
        res.id = problem.getId();
        res.levelId = problem.getLevelId();
        res.levelTitle = LevelCatalog.findById(problem.getLevelId())
                .map(level -> level.title())
                .orElse("");
        res.title = problem.getTitle();
        res.difficulty = problem.getDifficulty();
        res.description = problem.getDescription();
        res.favorite = problem.isFavorite();
        res.attempted = attempted;
        res.correct = correct;
        res.createdAt = problem.getCreatedAt();
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public String getLevelTitle() {
        return levelTitle;
    }

    public void setLevelTitle(String levelTitle) {
        this.levelTitle = levelTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean isAttempted() {
        return attempted;
    }

    public void setAttempted(boolean attempted) {
        this.attempted = attempted;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
