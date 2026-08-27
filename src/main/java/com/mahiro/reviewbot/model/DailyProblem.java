package com.mahiro.reviewbot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 1日1問、Geminiが目標に合わせて生成するプログラミング問題。
 */
public class DailyProblem {

    private Long id;
    private LocalDate problemDate;
    private String title;
    private String difficulty;
    private String description;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getProblemDate() {
        return problemDate;
    }

    public void setProblemDate(LocalDate problemDate) {
        this.problemDate = problemDate;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
