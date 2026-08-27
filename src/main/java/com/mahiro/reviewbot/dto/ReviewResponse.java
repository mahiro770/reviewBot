package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.CodeReview;

import java.time.LocalDateTime;

/** レビュー結果としてフロントエンドに返すデータ */
public class ReviewResponse {

    private Long id;
    private String review;
    private Integer score;
    private LocalDateTime createdAt;
    private Long problemId;

    public ReviewResponse() {
    }

    public static ReviewResponse from(CodeReview review) {
        ReviewResponse res = new ReviewResponse();
        res.id = review.getId();
        res.review = review.getReview();
        res.score = review.getScore();
        res.createdAt = review.getCreatedAt();
        res.problemId = review.getProblemId();
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
