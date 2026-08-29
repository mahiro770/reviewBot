package com.mahiro.reviewbot.repository;

import com.mahiro.reviewbot.model.Level;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * levels テーブル(問題集のカリキュラム)へのアクセスを担当するRepository。
 * 内容は db/migration/V2__levels.sql で投入され、直接編集すれば変更できる。
 */
@Repository
public class LevelRepository {

    private final JdbcTemplate jdbcTemplate;

    public LevelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Level> findAll() {
        String sql = "SELECT id, title, certification, topic_hint FROM levels ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRow);
    }

    public Optional<Level> findById(int id) {
        String sql = "SELECT id, title, certification, topic_hint FROM levels WHERE id = ?";
        List<Level> results = jdbcTemplate.query(sql, this::mapRow, id);
        return results.stream().findFirst();
    }

    private Level mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Level(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("certification"),
                rs.getString("topic_hint")
        );
    }
}
