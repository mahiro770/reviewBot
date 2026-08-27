package com.mahiro.reviewbot.dto;

import com.mahiro.reviewbot.model.Goal;

import java.time.LocalDate;

/** 現在の目標としてフロントエンドに返すデータ */
public class GoalResponse {

    private Long id;
    private String targetVision;
    private String buildTarget;
    private Integer dailyMinutes;
    private LocalDate startDate;
    private LocalDate targetDate;
    private Long daysRemaining;
    private Integer progressPercent;

    public static GoalResponse from(Goal goal) {
        GoalResponse res = new GoalResponse();
        res.id = goal.getId();
        res.targetVision = goal.getTargetVision();
        res.buildTarget = goal.getBuildTarget();
        res.dailyMinutes = goal.getDailyMinutes();
        res.startDate = goal.getStartDate();
        res.targetDate = goal.getTargetDate();

        LocalDate today = LocalDate.now();
        if (goal.getTargetDate() != null) {
            res.daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, goal.getTargetDate());

            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(goal.getStartDate(), goal.getTargetDate());
            long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(goal.getStartDate(), today);
            if (totalDays > 0) {
                int percent = (int) Math.round(elapsedDays * 100.0 / totalDays);
                res.progressPercent = Math.max(0, Math.min(100, percent));
            }
        }
        return res;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTargetVision() {
        return targetVision;
    }

    public void setTargetVision(String targetVision) {
        this.targetVision = targetVision;
    }

    public String getBuildTarget() {
        return buildTarget;
    }

    public void setBuildTarget(String buildTarget) {
        this.buildTarget = buildTarget;
    }

    public Integer getDailyMinutes() {
        return dailyMinutes;
    }

    public void setDailyMinutes(Integer dailyMinutes) {
        this.dailyMinutes = dailyMinutes;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
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
}
