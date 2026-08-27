package com.mahiro.reviewbot.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学習目標を表すモデル。常に最新の1件が「現在の目標」として扱われる。
 */
public class Goal {

    private Long id;
    private String targetVision;
    private String buildTarget;
    private Integer dailyMinutes;
    private LocalDate startDate;
    private LocalDate targetDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
