package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.Level;
import com.mahiro.reviewbot.service.LevelProgressService;

/** 問題集のレベル一覧としてフロントエンドに返すデータ */
public class LevelResponse {

    private int id;
    private String title;
    private String certification;
    private boolean cleared;
    private int correctCount;
    private int requiredCorrectCount;

    public static LevelResponse from(Level level, int correctCount) {
        LevelResponse res = new LevelResponse();
        res.id = level.id();
        res.title = level.title();
        res.certification = level.certification();
        res.correctCount = correctCount;
        res.requiredCorrectCount = LevelProgressService.REQUIRED_CORRECT_TO_CLEAR;
        res.cleared = correctCount >= LevelProgressService.REQUIRED_CORRECT_TO_CLEAR;
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

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getRequiredCorrectCount() {
        return requiredCorrectCount;
    }

    public void setRequiredCorrectCount(int requiredCorrectCount) {
        this.requiredCorrectCount = requiredCorrectCount;
    }
}
