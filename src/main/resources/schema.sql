CREATE TABLE IF NOT EXISTS code_reviews (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    code       TEXT    NOT NULL,
    review     TEXT    NOT NULL,
    score      INTEGER,
    created_at TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS goals (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    target_vision TEXT    NOT NULL,
    build_target  TEXT,
    daily_minutes INTEGER,
    start_date    TEXT    NOT NULL,
    target_date   TEXT,
    created_at    TEXT    NOT NULL,
    updated_at    TEXT    NOT NULL
);

-- 「今日の問題」(1日1問の自動出題)機能は「問題集」(レベル別出題)に統合されて廃止された
DROP TABLE IF EXISTS daily_problems;

CREATE TABLE IF NOT EXISTS problems (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    level_id     INTEGER NOT NULL,
    title        TEXT    NOT NULL,
    difficulty   TEXT,
    description  TEXT    NOT NULL,
    is_favorite  INTEGER NOT NULL DEFAULT 0,
    created_at   TEXT    NOT NULL
);

-- 既存DBに対する追加カラム。継続稼働中のDBでは2回目以降エラーになるが
-- spring.sql.init.continue-on-error=true により無視され、実質的なマイグレーションとして機能する。
ALTER TABLE code_reviews ADD COLUMN problem_id INTEGER REFERENCES problems(id);
ALTER TABLE code_reviews ADD COLUMN is_correct INTEGER;
