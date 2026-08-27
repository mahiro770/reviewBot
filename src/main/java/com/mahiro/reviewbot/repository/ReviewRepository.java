package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.CodeReview;
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
 * code_reviews テーブルへのアクセスを担当するRepository。
 * JPAは使わず、あえてJdbcTemplateで素直にSQLを書いている
 * (JDBC + SQLiteの経験をそのまま活かせるように)。
 */
@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CodeReview save(CodeReview review) {
        String sql = "INSERT INTO code_reviews (code, review, score, created_at, problem_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getCode());
            ps.setString(2, review.getReview());
            if (review.getScore() != null) {
                ps.setInt(3, review.getScore());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, Timestamp.valueOf(review.getCreatedAt()).toString());
            if (review.getProblemId() != null) {
                ps.setLong(5, review.getProblemId());
            } else {
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            review.setId(key.longValue());
        }
        return review;
    }

    public List<CodeReview> findAllOrderByCreatedAtDesc() {
        String sql = "SELECT id, code, review, score, created_at, problem_id FROM code_reviews ORDER BY id DESC";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    public Optional<CodeReview> findById(long id) {
        String sql = "SELECT id, code, review, score, created_at, problem_id FROM code_reviews WHERE id = ?";
        List<CodeReview> results = jdbcTemplate.query(sql, this::mapRow, id);
        return results.stream().findFirst();
    }

    public boolean existsByProblemId(long problemId) {
        String sql = "SELECT COUNT(*) FROM code_reviews WHERE problem_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, problemId);
        return count != null && count > 0;
    }

    private CodeReview mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        CodeReview review = new CodeReview();
        review.setId(rs.getLong("id"));
        review.setCode(rs.getString("code"));
        review.setReview(rs.getString("review"));
        int score = rs.getInt("score");
        review.setScore(rs.wasNull() ? null : score);
        review.setCreatedAt(LocalDateTime.parse(
                rs.getString("created_at").replace(" ", "T")
        ));
        long problemId = rs.getLong("problem_id");
        review.setProblemId(rs.wasNull() ? null : problemId);
        return review;
    }
}
