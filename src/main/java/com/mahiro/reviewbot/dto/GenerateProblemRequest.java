package com.mahiro.reviewbot.dto;

/** 新しい問題を生成するリクエスト */
public class GenerateProblemRequest {

    private int levelId;
    private int count;

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
