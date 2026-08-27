package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.dto.GoalRequest;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 目標(goals)の読み書きを担当するService。
 */
@Service
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }

    public Optional<Goal> getCurrentGoal() {
        return goalRepository.findLatest();
    }

    public Goal saveGoal(GoalRequest request) {
        if (request.getTargetVision() == null || request.getTargetVision().isBlank()) {
            throw new IllegalArgumentException("目指す姿を入力してください。");
        }

        Goal goal = new Goal();
        goal.setTargetVision(request.getTargetVision());
        goal.setBuildTarget(request.getBuildTarget());
        goal.setDailyMinutes(request.getDailyMinutes());
        goal.setStartDate(request.getStartDate() != null && !request.getStartDate().isBlank()
                ? LocalDate.parse(request.getStartDate())
                : LocalDate.now());
        goal.setTargetDate(request.getTargetDate() != null && !request.getTargetDate().isBlank()
                ? LocalDate.parse(request.getTargetDate())
                : null);

        LocalDateTime now = LocalDateTime.now();
        goal.setCreatedAt(now);
        goal.setUpdatedAt(now);

        return goalRepository.save(goal);
    }
}
