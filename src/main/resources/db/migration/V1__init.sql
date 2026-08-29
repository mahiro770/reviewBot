-- 現在の最終スキーマをベースラインとして定義したもの。
-- 既存のreview.db(旧schema.sqlの継ぎ足しで作られたDB)に対して実行しても、
-- 全テーブルがCREATE TABLE IF NOT EXISTSのため何も変わらず安全に「バージョン1適用済み」の
-- 記録だけが残る。新規にreview.dbを作る場合は、ここで全テーブルが1から作られる。

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

CREATE TABLE IF NOT EXISTS problems (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    level_id     INTEGER NOT NULL,
    title        TEXT    NOT NULL,
    difficulty   TEXT,
    description  TEXT    NOT NULL,
    is_favorite  INTEGER NOT NULL DEFAULT 0,
    created_at   TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS code_reviews (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    code       TEXT    NOT NULL,
    review     TEXT    NOT NULL,
    score      INTEGER,
    created_at TEXT    NOT NULL,
    problem_id INTEGER REFERENCES problems(id),
    is_correct INTEGER
);
