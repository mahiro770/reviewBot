package com.mahiro.reviewbot.dto;

import java.util.List;

/** 進捗タブに表示する集計データ */
public class StatsResponse {

    /** 日付ごとの平均スコア(時系列、古い順) */
    public record ScorePoint(String date, double averageScore) {
    }

    /** 日付ごとのレビュー提出件数(直近30日、古い順) */
    public record ActivityPoint(String date, int count) {
    }

    private List<ScorePoint> scoreTrend;
    private List<ActivityPoint> dailyActivity;
    private int streakDays;
    private int totalReviews;
    private Double averageScore;
    private Long daysRemaining;
    private Integer progressPercent;
    private int silverCleared;
    private int silverTotal;
    private int goldCleared;
    private int goldTotal;

    public List<ScorePoint> getScoreTrend() {
        return scoreTrend;
    }

    public void setScoreTrend(List<ScorePoint> scoreTrend) {
        this.scoreTrend = scoreTrend;
    }

    public List<ActivityPoint> getDailyActivity() {
        return dailyActivity;
    }

    public void setDailyActivity(List<ActivityPoint> dailyActivity) {
        this.dailyActivity = dailyActivity;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(int streakDays) {
        this.streakDays = streakDays;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Double averageScore) {
        this.averageScore = averageScore;
    }

    public Long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(Long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    public int getSilverCleared() {
        return silverCleared;
    }

    public void setSilverCleared(int silverCleared) {
        this.silverCleared = silverCleared;
    }

    public int getSilverTotal() {
        return silverTotal;
    }

    public void setSilverTotal(int silverTotal) {
        this.silverTotal = silverTotal;
    }

    public int getGoldCleared() {
        return goldCleared;
    }

    public void setGoldCleared(int goldCleared) {
        this.goldCleared = goldCleared;
    }

    public int getGoldTotal() {
        return goldTotal;
    }

    public void setGoldTotal(int goldTotal) {
        this.goldTotal = goldTotal;
    }
}
