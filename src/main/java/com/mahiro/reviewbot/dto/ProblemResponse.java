package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.DailyProblem;

import java.time.LocalDate;

/** 出題された問題としてフロントエンドに返すデータ */
public class ProblemResponse {

    private Long id;
    private LocalDate problemDate;
    private String title;
    private String difficulty;
    private String description;
    private boolean solved;

    public static ProblemResponse from(DailyProblem problem, boolean solved) {
        ProblemResponse res = new ProblemResponse();
        res.id = problem.getId();
        res.problemDate = problem.getProblemDate();
        res.title = problem.getTitle();
        res.difficulty = problem.getDifficulty();
        res.description = problem.getDescription();
        res.solved = solved;
        return res;
    }

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

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }
}
