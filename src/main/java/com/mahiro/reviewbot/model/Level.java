package com.mahiro.reviewbot.model;

/**
 * 問題集の1レベル。Java Silver / Gold(Oracle認定資格)の出題範囲を参考にしたカリキュラム。
 * levels テーブルに保存されており、コードを変更しなくても行を編集すれば内容を変更できる。
 *
 * @param id            レベルID(1始まり)
 * @param title         レベル名
 * @param certification "SILVER" または "GOLD"
 * @param topicHint     AIに問題を生成させる際に渡す、このレベルで扱うべきテーマの詳細指示
 */
public record Level(int id, String title, String certification, String topicHint) {

    public static final String SILVER = "SILVER";
    public static final String GOLD = "GOLD";
}
