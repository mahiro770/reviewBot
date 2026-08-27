package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.dto.ClaudeMessage;
import com.mahiro.reviewbot.dto.ClaudeRequest;
import com.mahiro.reviewbot.dto.ClaudeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Claude(Anthropic Messages API)を呼び出してJavaコードのレビューをしてもらうサービス。
 */
@Service
public class ClaudeReviewService {

    private static final String SYSTEM_PROMPT = """
            あなたは経験豊富なシニアJavaエンジニア兼メンターです。
            未経験からITエンジニアに転向し、現在Java/Spring Bootを学習中のエンジニアが
            書いたJavaコードをレビューしてください。

            以下の観点で、日本語で具体的にコメントしてください。
            1. バグ・不具合の可能性(nullチェック漏れ、例外処理、境界値など)
            2. 設計・可読性(命名、責務の分割、メソッドの長さなど)
            3. Javaのベストプラクティス・イディオムに沿っているか
            4. 良い点(できていることもきちんと褒める)
            5. 次に学ぶと良いこと・改善提案を1〜2個

            厳しすぎず、しかし妥協せずに、学習者が次に何をすべきか分かるように書いてください。
            出力の一番最後の行は、必ず次の形式のみで終えてください(他の文字を含めない):
            スコア: XX/100
            """;

    private final RestTemplate restTemplate;

    @Value("${claude.api.key}")
    private String apiKey;

    @Value("${claude.api.url}")
    private String apiUrl;

    @Value("${claude.api.model}")
    private String model;

    @Value("${claude.api.max-tokens}")
    private int maxTokens;

    private static final Pattern SCORE_PATTERN = Pattern.compile("スコア[:：]\\s*(\\d{1,3})\\s*/\\s*100");

    public ClaudeReviewService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record ReviewResult(String reviewText, Integer score) {
    }

    public ReviewResult reviewCode(String code) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY が設定されていません。環境変数にAPIキーを設定して起動し直してください。");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("レビューするコードが空です。");
        }

        ClaudeRequest request = new ClaudeRequest(
                model,
                maxTokens,
                SYSTEM_PROMPT,
                List.of(new ClaudeMessage("user", "次のJavaコードをレビューしてください:\n\n```java\n" + code + "\n```"))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        HttpEntity<ClaudeRequest> entity = new HttpEntity<>(request, headers);
        ClaudeResponse response = restTemplate.postForObject(apiUrl, entity, ClaudeResponse.class);

        if (response == null || response.getContent() == null || response.getContent().isEmpty()) {
            throw new IllegalStateException("Claude APIから空のレスポンスが返ってきました。");
        }

        String text = response.getContent().stream()
                .filter(block -> "text".equals(block.getType()))
                .map(block -> block.getText())
                .findFirst()
                .orElse("");

        return new ReviewResult(text, extractScore(text));
    }

    private Integer extractScore(String text) {
        Matcher matcher = SCORE_PATTERN.matcher(text);
        Integer last = null;
        while (matcher.find()) {
            last = Integer.parseInt(matcher.group(1));
        }
        return last;
    }
}
