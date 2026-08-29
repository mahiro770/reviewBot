package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.CodeReview;

import java.time.LocalDateTime;

/** 履歴一覧に表示する、コードの先頭だけを含む軽量なサマリー */
public class ReviewSummary {

    private Long id;
    private String codePreview;
    private Integer score;
    private LocalDateTime createdAt;
    private Boolean isCorrect;

    public static ReviewSummary from(CodeReview review) {
        ReviewSummary summary = new ReviewSummary();
        summary.id = review.getId();
        String code = review.getCode() == null ? "" : review.getCode();
        summary.codePreview = code.length() > 80 ? code.substring(0, 80) + "..." : code;
        summary.score = review.getScore();
        summary.createdAt = review.getCreatedAt();
        summary.isCorrect = review.getIsCorrect();
        return summary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePreview() {
        return codePreview;
    }

    public void setCodePreview(String codePreview) {
        this.codePreview = codePreview;
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

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
