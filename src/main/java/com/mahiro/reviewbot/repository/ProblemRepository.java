package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.DailyProblem;
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
 * daily_problems テーブルへのアクセスを担当するRepository。
 */
@Repository
public class ProblemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProblemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public DailyProblem save(DailyProblem problem) {
        String sql = "INSERT INTO daily_problems (problem_date, title, difficulty, description, created_at) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, problem.getProblemDate().toString());
            ps.setString(2, problem.getTitle());
            ps.setString(3, problem.getDifficulty());
            ps.setString(4, problem.getDescription());
            ps.setString(5, Timestamp.valueOf(problem.getCreatedAt()).toString());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            problem.setId(key.longValue());
        }
        return problem;
    }

    public Optional<DailyProblem> findByDate(LocalDate date) {
        String sql = "SELECT id, problem_date, title, difficulty, description, created_at FROM daily_problems WHERE problem_date = ?";
        List<DailyProblem> results = jdbcTemplate.query(sql, this::mapRow, date.toString());
        return results.stream().findFirst();
    }

    public Optional<DailyProblem> findById(long id) {
        String sql = "SELECT id, problem_date, title, difficulty, description, created_at FROM daily_problems WHERE id = ?";
        List<DailyProblem> results = jdbcTemplate.query(sql, this::mapRow, id);
        return results.stream().findFirst();
    }

    public List<String> findRecentTitles(int limit) {
        String sql = "SELECT title FROM daily_problems ORDER BY id DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, String.class, limit);
    }

    public List<DailyProblem> findAllOrderByDateDesc() {
        String sql = "SELECT id, problem_date, title, difficulty, description, created_at FROM daily_problems ORDER BY problem_date DESC";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    private DailyProblem mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        DailyProblem problem = new DailyProblem();
        problem.setId(rs.getLong("id"));
        problem.setProblemDate(LocalDate.parse(rs.getString("problem_date")));
        problem.setTitle(rs.getString("title"));
        problem.setDifficulty(rs.getString("difficulty"));
        problem.setDescription(rs.getString("description"));
        problem.setCreatedAt(LocalDateTime.parse(rs.getString("created_at").replace(" ", "T")));
        return problem;
    }
}
