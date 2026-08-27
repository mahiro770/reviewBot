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

CREATE TABLE IF NOT EXISTS daily_problems (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    problem_date TEXT    NOT NULL UNIQUE,
    title        TEXT    NOT NULL,
    difficulty   TEXT,
    description  TEXT    NOT NULL,
    created_at   TEXT    NOT NULL
);

-- 既存DBに対する追加カラム。継続稼働中のDBでは2回目以降エラーになるが
-- spring.sql.init.continue-on-error=true により無視され、実質的なマイグレーションとして機能する。
ALTER TABLE code_reviews ADD COLUMN problem_id INTEGER REFERENCES daily_problems(id);
