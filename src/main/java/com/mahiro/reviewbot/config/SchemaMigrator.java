package com.mahiro.reviewbot.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自作の簡易マイグレーション管理。
 *
 * Flywayを使いたかったが、Flyway 10ではSQLite対応が無料のMaven Central配布に
 * 含まれていないため使えなかった。代わりに、`db/migration/V数字__説明.sql` という
 * ファイルを番号順に1回ずつ適用し、適用済みバージョンを schema_version テーブルに
 * 記録するだけの最小限の仕組みをここで用意している。
 * (以前の schema.sql + continue-on-error 方式と違い、「本当に失敗した」場合は
 * 例外がそのまま起動を止めるので、問題を握りつぶさない)
 */
@Component
public class SchemaMigrator implements ApplicationRunner {

    private static final Pattern FILE_PATTERN = Pattern.compile("V(\\d+)__(.+)\\.sql");

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrator(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    private record Migration(int version, String description, Resource resource) {
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS schema_version (
                    version     INTEGER PRIMARY KEY,
                    description TEXT NOT NULL,
                    applied_at  TEXT NOT NULL
                )
                """);

        int currentVersion = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version), 0) FROM schema_version", Integer.class);

        List<Migration> pending = discoverMigrations().stream()
                .filter(m -> m.version() > currentVersion)
                .sorted(Comparator.comparingInt(Migration::version))
                .toList();

        for (Migration migration : pending) {
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, migration.resource());
            }
            jdbcTemplate.update(
                    "INSERT INTO schema_version (version, description, applied_at) VALUES (?, ?, ?)",
                    migration.version(), migration.description(), LocalDateTime.now().toString());
        }
    }

    private List<Migration> discoverMigrations() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:db/migration/V*.sql");

        List<Migration> migrations = new ArrayList<>();
        for (Resource resource : resources) {
            Matcher matcher = FILE_PATTERN.matcher(resource.getFilename());
            if (matcher.matches()) {
                int version = Integer.parseInt(matcher.group(1));
                String description = matcher.group(2).replace("_", " ");
                migrations.add(new Migration(version, description, resource));
            }
        }
        return migrations;
    }
}
