package com.mahiro.reviewbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahiro.reviewbot.dto.GeminiContent;
import com.mahiro.reviewbot.dto.GeminiGenerationConfig;
import com.mahiro.reviewbot.dto.GeminiRequest;
import com.mahiro.reviewbot.dto.GeminiSchema;
import com.mahiro.reviewbot.model.Problem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Gemini(Google Generative Language API)を呼び出してJavaコードのレビューをしてもらうサービス。
 * Gemini APIは無料枠があるため、学習用途でのAPI利用コストを抑えられる。
 * responseSchemaで構造化出力(JSON)を強制するため、自由記述からの正規表現抽出は行わない。
 * 実際のHTTP呼び出し・リトライは GeminiClient に任せている。
 */
@Service
public class GeminiReviewService {

    private static final String SYSTEM_PROMPT = """
            あなたは経験豊富なシニアJavaエンジニア兼メンターです。
            未経験からITエンジニアに転向し、現在Java/Spring Bootを学習中のエンジニアが
            書いたJavaコードをレビューしてください。

            review フィールドには、以下の観点を日本語で具体的に含めてください。
            1. バグ・不具合の可能性(nullチェック漏れ、例外処理、境界値など)
            2. 設計・可読性(命名、責務の分割、メソッドの長さなど)
            3. Javaのベストプラクティス・イディオムに沿っているか
            4. 良い点(できていることもきちんと褒める)
            5. 次に学ぶと良いこと・改善提案を1〜2個

            厳しすぎず、しかし妥協せずに、学習者が次に何をすべきか分かるように書いてください。
            score フィールドには0〜100の整数でスコアをつけてください。
            """;

    private static final String JUDGEMENT_INSTRUCTION = """

            この回答は、以下の問題に対する提出です。問題の要件を満たしていれば correct を true、
            満たしていなければ false にしてください。
            """;

    private static final GeminiSchema PLAIN_SCHEMA = GeminiSchema.object(
            Map.of("review", GeminiSchema.string(), "score", GeminiSchema.integer()),
            List.of("review", "score")
    );

    private static final GeminiSchema JUDGED_SCHEMA = GeminiSchema.object(
            Map.of("review", GeminiSchema.string(), "score", GeminiSchema.integer(), "correct", GeminiSchema.bool()),
            List.of("review", "score", "correct")
    );

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.max-tokens}")
    private int maxTokens;

    public GeminiReviewService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public record ReviewResult(String reviewText, Integer score, Boolean correct) {
    }

    /** レビューJSONの構造化出力をそのまま受け取るための内部DTO(problem == null時はcorrectはnullのまま) */
    private record ReviewJson(String review, Integer score, Boolean correct) {
    }

    /** アドホックなレビュー(問題との紐付けなし)。正誤判定は行わない */
    public ReviewResult reviewCode(String code) {
        return reviewCode(code, null);
    }

    /** problem が指定されている場合は、その問題の要件を満たしているかも判定させる */
    public ReviewResult reviewCode(String code, Problem problem) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("レビューするコードが空です。");
        }

        String systemPrompt = SYSTEM_PROMPT;
        String userMessage;
        if (problem != null) {
            systemPrompt += JUDGEMENT_INSTRUCTION;
            userMessage = "次の問題と、それに対する回答Javaコードをレビューしてください:\n\n" +
                    "## 問題\n" + problem.getDescription() + "\n\n" +
                    "## 回答コード\n```java\n" + code + "\n```";
        } else {
            userMessage = "次のJavaコードをレビューしてください:\n\n```java\n" + code + "\n```";
        }

        GeminiSchema schema = problem != null ? JUDGED_SCHEMA : PLAIN_SCHEMA;
        GeminiRequest request = new GeminiRequest(
                List.of(GeminiContent.ofText("user", userMessage)),
                GeminiContent.ofText(null, systemPrompt),
                new GeminiGenerationConfig(maxTokens, schema)
        );

        String text = geminiClient.generateText(request);

        ReviewJson parsed;
        try {
            parsed = objectMapper.readValue(text, ReviewJson.class);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini APIの応答を解析できませんでした: " + e.getMessage());
        }

        return new ReviewResult(parsed.review(), parsed.score(), problem != null ? parsed.correct() : null);
    }
}
