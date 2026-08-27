package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.Goal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * goals テーブルへのアクセスを担当するRepository。
 * 「常に最新の1件が現在の目標」という運用のため、findLatest で十分。
 */
@Repository
public class GoalRepository {

    private final JdbcTemplate jdbcTemplate;

    public GoalRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Goal save(Goal goal) {
        String sql = "INSERT INTO goals (target_vision, build_target, daily_minutes, start_date, target_date, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, goal.getTargetVision());
            ps.setString(2, goal.getBuildTarget());
            if (goal.getDailyMinutes() != null) {
                ps.setInt(3, goal.getDailyMinutes());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, goal.getStartDate().toString());
            ps.setString(5, goal.getTargetDate() != null ? goal.getTargetDate().toString() : null);
            ps.setString(6, Timestamp.valueOf(goal.getCreatedAt()).toString());
            ps.setString(7, Timestamp.valueOf(goal.getUpdatedAt()).toString());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            goal.setId(key.longValue());
        }
        return goal;
    }

    public Optional<Goal> findLatest() {
        String sql = "SELECT id, target_vision, build_target, daily_minutes, start_date, target_date, created_at, updated_at "
                + "FROM goals ORDER BY id DESC LIMIT 1";
        List<Goal> results = jdbcTemplate.query(sql, this::mapRow);
        return results.stream().findFirst();
    }

    private Goal mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getLong("id"));
        goal.setTargetVision(rs.getString("target_vision"));
        goal.setBuildTarget(rs.getString("build_target"));
        int dailyMinutes = rs.getInt("daily_minutes");
        goal.setDailyMinutes(rs.wasNull() ? null : dailyMinutes);
        goal.setStartDate(LocalDate.parse(rs.getString("start_date")));
        String targetDate = rs.getString("target_date");
        goal.setTargetDate(targetDate != null ? LocalDate.parse(targetDate) : null);
        goal.setCreatedAt(LocalDateTime.parse(rs.getString("created_at").replace(" ", "T")));
        goal.setUpdatedAt(LocalDateTime.parse(rs.getString("updated_at").replace(" ", "T")));
        return goal;
    }
}
