package com.mahiro.reviewbot.controller;

import com.mahiro.reviewbot.dto.LevelResponse;
import com.mahiro.reviewbot.repository.LevelRepository;
import com.mahiro.reviewbot.service.LevelProgressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Controller層: 問題集のレベル一覧(Java Silver/Gold準拠のカリキュラム)を返す。
 */
@RestController
@RequestMapping("/api/levels")
public class LevelController {

    private final LevelRepository levelRepository;
    private final LevelProgressService levelProgressService;

    public LevelController(LevelRepository levelRepository, LevelProgressService levelProgressService) {
        this.levelRepository = levelRepository;
        this.levelProgressService = levelProgressService;
    }

    @GetMapping
    public List<LevelResponse> listLevels() {
        Map<Integer, Integer> correctCounts = levelProgressService.correctCountByLevel();

        return levelRepository.findAll().stream()
                .map(level -> LevelResponse.from(level, correctCounts.getOrDefault(level.id(), 0)))
                .toList();
    }
}
