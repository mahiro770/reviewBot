package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.dto.GeminiContent;
import com.mahiro.reviewbot.dto.GeminiGenerationConfig;
import com.mahiro.reviewbot.dto.GeminiRequest;
import com.mahiro.reviewbot.dto.GeminiResponse;
import com.mahiro.reviewbot.model.Problem;
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
 * Gemini(Google Generative Language API)を呼び出してJavaコードのレビューをしてもらうサービス。
 * Gemini APIは無料枠があるため、学習用途でのAPI利用コストを抑えられる。
 */
@Service
public class GeminiReviewService {

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

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.max-tokens}")
    private int maxTokens;

    private static final Pattern SCORE_PATTERN = Pattern.compile("スコア[:：]\\s*(\\d{1,3})\\s*/\\s*100");
    private static final Pattern JUDGEMENT_PATTERN = Pattern.compile("判定[:：]\\s*(正解|不正解)");

    public GeminiReviewService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record ReviewResult(String reviewText, Integer score, Boolean correct) {
    }

    /** アドホックなレビュー(問題との紐付けなし)。正誤判定は行わない */
    public ReviewResult reviewCode(String code) {
        return reviewCode(code, null);
    }

    /** problem が指定されている場合は、その問題の要件を満たしているかも判定させる */
    public ReviewResult reviewCode(String code, Problem problem) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY が設定されていません。環境変数にAPIキーを設定して起動し直してください。");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("レビューするコードが空です。");
        }

        String systemPrompt = SYSTEM_PROMPT;
        String userMessage;
        if (problem != null) {
            systemPrompt += """

                    この回答は、以下の問題に対する提出です。問題の要件を満たしているかどうかも判定してください。
                    出力の最後から2行目に、必ず次の形式のみで判定を書いてください(他の文字を含めない):
                    判定: 正解 または 判定: 不正解
                    (最後の行は、これまで通り「スコア: XX/100」のままにしてください)
                    """;
            userMessage = "次の問題と、それに対する回答Javaコードをレビューしてください:\n\n" +
                    "## 問題\n" + problem.getDescription() + "\n\n" +
                    "## 回答コード\n```java\n" + code + "\n```";
        } else {
            userMessage = "次のJavaコードをレビューしてください:\n\n```java\n" + code + "\n```";
        }

        GeminiRequest request = new GeminiRequest(
                List.of(GeminiContent.ofText("user", userMessage)),
                GeminiContent.ofText(null, systemPrompt),
                new GeminiGenerationConfig(maxTokens)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
        String url = baseUrl + "/" + model + ":generateContent";
        GeminiResponse response = restTemplate.postForObject(url, entity, GeminiResponse.class);

        String text = extractText(response);
        if (text.isBlank()) {
            throw new IllegalStateException("Gemini APIから空のレスポンスが返ってきました。");
        }

        Boolean correct = problem != null ? extractJudgement(text) : null;
        return new ReviewResult(text, extractScore(text), correct);
    }

    private String extractText(GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            return "";
        }
        GeminiContent content = response.getCandidates().get(0).getContent();
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            return "";
        }
        return content.getParts().stream()
                .map(part -> part.getText() == null ? "" : part.getText())
                .reduce("", String::concat);
    }

    private Integer extractScore(String text) {
        Matcher matcher = SCORE_PATTERN.matcher(text);
        Integer last = null;
        while (matcher.find()) {
            last = Integer.parseInt(matcher.group(1));
        }
        return last;
    }

    private Boolean extractJudgement(String text) {
        Matcher matcher = JUDGEMENT_PATTERN.matcher(text);
        Boolean last = null;
        while (matcher.find()) {
            last = "正解".equals(matcher.group(1));
        }
        return last;
    }
}
