-- 問題集のカリキュラムをJavaコード埋め込み(LevelCatalog)からDBに移す。
-- コードを変更・再デプロイしなくても、このテーブルを直接編集すればレベルを追加/変更できる。

CREATE TABLE IF NOT EXISTS levels (
    id            INTEGER PRIMARY KEY,
    title         TEXT    NOT NULL,
    certification TEXT    NOT NULL,
    topic_hint    TEXT    NOT NULL
);

INSERT INTO levels (id, title, certification, topic_hint) VALUES
    (1, '変数・データ型・演算子', 'SILVER', 'プリミティブ型、型変換、算術・比較・論理演算子、演算子の優先順位を扱う基礎問題'),
    (2, '制御構文(if/switch/for/while)', 'SILVER', 'if/switch文、for/拡張for/while/do-while文、break/continueによる制御フローを扱う問題'),
    (3, '配列とArrayList', 'SILVER', '1次元/多次元配列の宣言と操作、ArrayListの基本操作(add/remove/get/size)を扱う問題'),
    (4, 'クラス設計の基本(カプセル化・コンストラクタ)', 'SILVER', 'クラス定義、フィールドとメソッド、コンストラクタ、アクセス修飾子によるカプセル化を扱う問題'),
    (5, '継承とポリモーフィズム', 'SILVER', 'extends/implementsによる継承とインターフェース、メソッドのオーバーライド、ポリモーフィズムを扱う問題'),
    (6, '例外処理の基本(try-catch-finally)', 'SILVER', 'try-catch-finally、チェック例外と非チェック例外、独自例外クラスの基本を扱う問題'),
    (7, 'Stringとイミュータビリティ・StringBuilder', 'SILVER', 'Stringの不変性、文字列比較(==とequals)、StringBuilderによる文字列操作を扱う問題'),
    (8, 'ラムダ式と関数型インターフェース', 'GOLD', 'ラムダ式の構文、Predicate/Function/Consumer/Supplierなど標準関数型インターフェースを扱う問題'),
    (9, 'Stream API', 'GOLD', 'Streamの生成、filter/map/reduce/collectなどの中間・終端操作を扱う問題'),
    (10, '例外処理の応用とアサーション', 'GOLD', 'マルチキャッチ、try-with-resources、例外の連鎖(cause)、assert文を扱う問題'),
    (11, '日付・時刻API(java.time)', 'GOLD', 'LocalDate/LocalDateTime/Period/Duration/DateTimeFormatterなどjava.timeパッケージを扱う問題'),
    (12, '並行処理の基礎', 'GOLD', 'Threadとrunnable、ExecutorService、synchronizedによる排他制御の基礎を扱う問題');
