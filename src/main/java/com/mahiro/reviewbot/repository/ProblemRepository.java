package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.Problem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * problems テーブルへのアクセスを担当するRepository。
 */
@Repository
public class ProblemRepository {

    private final JdbcTemplate jdbcTemplate;

    public ProblemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Problem save(Problem problem) {
        String sql = "INSERT INTO problems (level_id, title, difficulty, description, is_favorite, created_at) VALUES (?, ?, ?, ?, 0, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, problem.getLevelId());
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

    public Optional<Problem> findById(long id) {
        String sql = "SELECT id, level_id, title, difficulty, description, is_favorite, created_at FROM problems WHERE id = ?";
        List<Problem> results = jdbcTemplate.query(sql, this::mapRow, id);
        return results.stream().findFirst();
    }

    public List<Problem> findByLevelId(int levelId) {
        String sql = "SELECT id, level_id, title, difficulty, description, is_favorite, created_at FROM problems WHERE level_id = ? ORDER BY id DESC";
        return jdbcTemplate.query(sql, this::mapRow, levelId);
    }

    public List<String> findRecentTitlesByLevel(int levelId, int limit) {
        String sql = "SELECT title FROM problems WHERE level_id = ? ORDER BY id DESC LIMIT ?";
        return jdbcTemplate.queryForList(sql, String.class, levelId, limit);
    }

    public List<Problem> findFavorites() {
        String sql = "SELECT id, level_id, title, difficulty, description, is_favorite, created_at FROM problems WHERE is_favorite = 1 ORDER BY id DESC";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    public List<Problem> findAll() {
        String sql = "SELECT id, level_id, title, difficulty, description, is_favorite, created_at FROM problems ORDER BY id DESC";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    public void setFavorite(long id, boolean favorite) {
        String sql = "UPDATE problems SET is_favorite = ? WHERE id = ?";
        jdbcTemplate.update(sql, favorite ? 1 : 0, id);
    }

    private Problem mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Problem problem = new Problem();
        problem.setId(rs.getLong("id"));
        problem.setLevelId(rs.getInt("level_id"));
        problem.setTitle(rs.getString("title"));
        problem.setDifficulty(rs.getString("difficulty"));
        problem.setDescription(rs.getString("description"));
        problem.setFavorite(rs.getInt("is_favorite") != 0);
        problem.setCreatedAt(LocalDateTime.parse(rs.getString("created_at").replace(" ", "T")));
        return problem;
    }
}
