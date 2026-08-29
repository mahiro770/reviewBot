package com.mahiro.reviewbot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
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
 *
 * ApplicationRunnerではなく@PostConstructにしているのは、
 * ApplicationRunnerは組み込みTomcatが接続を受け付け始めた後に実行されるため、
 * 起動直後の一瞬だけ未マイグレーションのDBにリクエストが飛びうる隙があるため。
 * @PostConstructはBean初期化(Tomcat起動より前)のタイミングで実行される。
 */
@Component
public class SchemaMigrator {

    private static final Pattern FILE_PATTERN = Pattern.compile("V(\\d+)__(.+)\\.sql");

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public SchemaMigrator(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    private record Migration(int version, String description, Resource resource) {
    }

    @PostConstruct
    public void migrate() {
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
                // Resourceをそのまま渡すとプラットフォームのデフォルト文字コード(Windowsでは
                // Shift-JIS系)で読まれ、UTF-8で書いたマイグレーションSQL中の日本語が文字化けする。
                // 明示的にUTF-8を指定する。
                ScriptUtils.executeSqlScript(connection, new EncodedResource(migration.resource(), StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new IllegalStateException(
                        "マイグレーション V" + migration.version() + " (" + migration.description() + ") に失敗しました", e);
            }
            jdbcTemplate.update(
                    "INSERT INTO schema_version (version, description, applied_at) VALUES (?, ?, ?)",
                    migration.version(), migration.description(), LocalDateTime.now().toString());
        }
    }

    private List<Migration> discoverMigrations() {
        try {
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
        } catch (Exception e) {
            throw new IllegalStateException("マイグレーションファイルの読み込みに失敗しました", e);
        }
    }
}
