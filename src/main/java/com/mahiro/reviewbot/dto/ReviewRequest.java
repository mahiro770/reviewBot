package com.mahiro.reviewbot.dto;

/** フロントエンドから送られてくる「レビューして欲しいコード」 */
public class ReviewRequest {

    private String code;
    private Long problemId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }
}
