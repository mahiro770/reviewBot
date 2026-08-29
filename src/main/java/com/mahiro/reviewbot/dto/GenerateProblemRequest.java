package com.mahiro.reviewbot.dto;

/** 新しい問題を生成するリクエスト */
public class GenerateProblemRequest {

    private int levelId;

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }
}
