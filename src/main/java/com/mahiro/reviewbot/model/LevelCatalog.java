package com.mahiro.reviewbot.model;

import java.util.List;
import java.util.Optional;

/**
 * 全12レベルの固定カリキュラム(Silver 7 + Gold 5)。DBではなくコード内の静的定義として持つ。
 */
public final class LevelCatalog {

    public static final String SILVER = "SILVER";
    public static final String GOLD = "GOLD";

    public static final List<Level> LEVELS = List.of(
            new Level(1, "変数・データ型・演算子", SILVER,
                    "プリミティブ型、型変換、算術・比較・論理演算子、演算子の優先順位を扱う基礎問題"),
            new Level(2, "制御構文(if/switch/for/while)", SILVER,
                    "if/switch文、for/拡張for/while/do-while文、break/continueによる制御フローを扱う問題"),
            new Level(3, "配列とArrayList", SILVER,
                    "1次元/多次元配列の宣言と操作、ArrayListの基本操作(add/remove/get/size)を扱う問題"),
            new Level(4, "クラス設計の基本(カプセル化・コンストラクタ)", SILVER,
                    "クラス定義、フィールドとメソッド、コンストラクタ、アクセス修飾子によるカプセル化を扱う問題"),
            new Level(5, "継承とポリモーフィズム", SILVER,
                    "extends/implementsによる継承とインターフェース、メソッドのオーバーライド、ポリモーフィズムを扱う問題"),
            new Level(6, "例外処理の基本(try-catch-finally)", SILVER,
                    "try-catch-finally、チェック例外と非チェック例外、独自例外クラスの基本を扱う問題"),
            new Level(7, "Stringとイミュータビリティ・StringBuilder", SILVER,
                    "Stringの不変性、文字列比較(==とequals)、StringBuilderによる文字列操作を扱う問題"),
            new Level(8, "ラムダ式と関数型インターフェース", GOLD,
                    "ラムダ式の構文、Predicate/Function/Consumer/Supplierなど標準関数型インターフェースを扱う問題"),
            new Level(9, "Stream API", GOLD,
                    "Streamの生成、filter/map/reduce/collectなどの中間・終端操作を扱う問題"),
            new Level(10, "例外処理の応用とアサーション", GOLD,
                    "マルチキャッチ、try-with-resources、例外の連鎖(cause)、assert文を扱う問題"),
            new Level(11, "日付・時刻API(java.time)", GOLD,
                    "LocalDate/LocalDateTime/Period/Duration/DateTimeFormatterなどjava.timeパッケージを扱う問題"),
            new Level(12, "並行処理の基礎", GOLD,
                    "Threadとrunnable、ExecutorService、synchronizedによる排他制御の基礎を扱う問題")
    );

    private LevelCatalog() {
    }

    public static Optional<Level> findById(int id) {
        return LEVELS.stream().filter(level -> level.id() == id).findFirst();
    }

    public static List<Level> byCertification(String certification) {
        return LEVELS.stream().filter(level -> level.certification().equals(certification)).toList();
    }
}
