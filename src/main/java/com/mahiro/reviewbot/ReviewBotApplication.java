package com.mahiro.reviewbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Javaコードレビューbot ― Claude APIを使って自分の書いたJavaコードを
 * レビューしてもらうための学習用Spring Bootアプリ。
 *
 * 起動すると http://localhost:8080 でフロントエンド(静的HTML)が開き、
 * コードを貼り付けて「レビューを依頼」ボタンを押すと
 * Controller -> Service -> (Claude API) / Repository(SQLite) -> Controller
 * という一連の流れが体験できる。
 */
@SpringBootApplication
public class ReviewBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewBotApplication.class, args);
    }
}
