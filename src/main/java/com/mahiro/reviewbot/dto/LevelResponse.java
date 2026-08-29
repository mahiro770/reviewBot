package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.Level;

/** 問題集のレベル一覧としてフロントエンドに返すデータ */
public class LevelResponse {

    private int id;
    private String title;
    private String certification;
    private boolean cleared;

    public static LevelResponse from(Level level, boolean cleared) {
        LevelResponse res = new LevelResponse();
        res.id = level.id();
        res.title = level.title();
        res.certification = level.certification();
        res.cleared = cleared;
        return res;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }
}
