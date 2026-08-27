package com.mahiro.reviewbot.dto;

/** フロントエンドから送られてくる目標設定フォームの内容 */
public class GoalRequest {

    private String targetVision;
    private String buildTarget;
    private Integer dailyMinutes;
    private String startDate;
    private String targetDate;

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }
}
