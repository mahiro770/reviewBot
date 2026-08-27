package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.GoalRequest;
import com.mahiro.reviewbot.dto.GoalResponse;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.service.GoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller層: 学習目標の取得・保存を扱う。
 */
@RestController
@RequestMapping("/api/goal")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /** 現在の目標を取得(未設定の場合は204) */
    @GetMapping
    public ResponseEntity<GoalResponse> getGoal() {
        return goalService.getCurrentGoal()
                .map(goal -> ResponseEntity.ok(GoalResponse.from(goal)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** 目標を新規作成/更新する */
    @PutMapping
    public ResponseEntity<?> saveGoal(@RequestBody GoalRequest request) {
        try {
            Goal saved = goalService.saveGoal(request);
            return ResponseEntity.ok(GoalResponse.from(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "目標の保存中にエラーが発生しました: " + e.getMessage()));
        }
    }
}
