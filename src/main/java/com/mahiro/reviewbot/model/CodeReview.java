package com.mahiro.reviewbot.model;

import java.time.LocalDateTime;

/**
 * 1回分のコードレビュー結果を表すモデル。
 * DB(code_reviewsテーブル)の1行に対応する。
 */
public class CodeReview {

    private Long id;
    private String code;
    private String review;
    private Integer score;
    private LocalDateTime createdAt;
    private Long problemId;
    private Boolean isCorrect;

    public CodeReview() {
    }

    public CodeReview(Long id, String code, String review, Integer score, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.review = review;
        this.score = score;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
