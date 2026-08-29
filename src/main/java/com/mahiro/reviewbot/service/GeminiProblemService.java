package com.mahiro.reviewbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahiro.reviewbot.dto.GeminiContent;
import com.mahiro.reviewbot.dto.GeminiGenerationConfig;
import com.mahiro.reviewbot.dto.GeminiRequest;
import com.mahiro.reviewbot.dto.GeminiResponse;
import com.mahiro.reviewbot.dto.GeminiSchema;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.model.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Gemini(Google Generative Language API)を呼び出して、指定レベルのカリキュラムに沿った
 * プログラミング問題を1問生成するサービス。
 * responseSchemaで構造化出力(JSON)を強制するため、自由記述からの正規表現抽出は行わない。
 */
@Service
public class GeminiProblemService {

    private static final String SYSTEM_PROMPT = """
            あなたは経験豊富なJavaメンターです。Java Silver / Gold(Oracle認定資格)の
            学習に取り組んでいるエンジニアに、指定されたレベルのテーマに沿った
            プログラミング問題を1問作成してください。

            以下の条件を守ってください。
            - 指定された「このレベルのテーマ」の範囲内の問題にする(範囲外の文法・APIを問わない)
            - 直近に出題した問題とテーマ・内容が重複しないようにする
            - 短時間(目安15〜30分)で解ける粒度にする(長すぎる大作にしない)
            - description フィールドには、要件・入出力の例を明確に書く(Markdown形式でよい)
            - 学習者の目標(分かっていれば)を、テーマを損なわない範囲で味付けとして活かしてよい
            - title フィールドには問題の短いタイトルを、difficulty フィールドには
              「初級」「中級」「上級」のいずれかを入れてください
            """;

    private static final GeminiSchema RESPONSE_SCHEMA = GeminiSchema.object(
            Map.of(
                    "title", GeminiSchema.string(),
                    "difficulty", GeminiSchema.stringEnum(List.of("初級", "中級", "上級")),
                    "description", GeminiSchema.string()
            ),
            List.of("title", "difficulty", "description")
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.max-tokens}")
    private int maxTokens;

    public GeminiProblemService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public record ProblemResult(String title, String difficulty, String description) {
    }

    public ProblemResult generateProblem(Level level, Goal goal, List<String> recentTitles) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY が設定されていません。環境変数にAPIキーを設定して起動し直してください。");
        }

        GeminiRequest request = new GeminiRequest(
                List.of(GeminiContent.ofText("user", buildUserMessage(level, goal, recentTitles))),
                GeminiContent.ofText(null, SYSTEM_PROMPT),
                new GeminiGenerationConfig(maxTokens, RESPONSE_SCHEMA)
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

        try {
            return objectMapper.readValue(text, ProblemResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Gemini APIの応答を解析できませんでした: " + e.getMessage());
        }
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

    private String buildUserMessage(Level level, Goal goal, List<String> recentTitles) {
        StringBuilder sb = new StringBuilder();
        sb.append("## レベル\n");
        sb.append("レベル").append(level.id()).append(": ").append(level.title())
                .append("(Java ").append(level.certification().equals("SILVER") ? "Silver" : "Gold").append(" 範囲)\n");
        sb.append("このレベルのテーマ: ").append(level.topicHint()).append("\n");

        if (goal != null) {
            sb.append("\n## 学習者の目標(参考、テーマ範囲を優先すること)\n");
            sb.append("目指す姿: ").append(goal.getTargetVision()).append("\n");
            if (goal.getBuildTarget() != null && !goal.getBuildTarget().isBlank()) {
                sb.append("作りたいもの: ").append(goal.getBuildTarget()).append("\n");
            }
        }

        if (recentTitles != null && !recentTitles.isEmpty()) {
            sb.append("\n## このレベルで直近に出題済みの問題タイトル(これらと重複しないテーマにする)\n");
            recentTitles.forEach(title -> sb.append("- ").append(title).append("\n"));
        }

        sb.append("\nこのレベルの問題を1問作成してください。");
        return sb.toString();
    }
}
