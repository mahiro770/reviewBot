package com.mahiro.reviewbot.model;

/**
 * 問題集の1レベル。Java Silver / Gold(Oracle認定資格)の出題範囲を参考にした固定カリキュラム。
 *
 * @param id            レベルID(1始まり)
 * @param title         レベル名
 * @param certification "SILVER" または "GOLD"
 * @param topicHint     AIに問題を生成させる際に渡す、このレベルで扱うべきテーマの詳細指示
 */
public record Level(int id, String title, String certification, String topicHint) {
}
