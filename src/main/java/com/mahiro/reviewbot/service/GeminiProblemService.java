package com.mahiro.reviewbot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahiro.reviewbot.dto.GeminiContent;
import com.mahiro.reviewbot.dto.GeminiGenerationConfig;
import com.mahiro.reviewbot.dto.GeminiRequest;
import com.mahiro.reviewbot.dto.GeminiSchema;
import com.mahiro.reviewbot.model.Goal;
import com.mahiro.reviewbot.model.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Gemini(Google Generative Language API)を呼び出して、指定レベルのカリキュラムに沿った
 * プログラミング問題をまとめて生成するサービス。
 * responseSchemaで構造化出力(JSON配列)を強制するため、自由記述からの正規表現抽出は行わない。
 * 実際のHTTP呼び出し・リトライは GeminiClient に任せている。
 */
@Service
public class GeminiProblemService {

    /** 1問あたりの目安トークン数。生成する問題数に応じてmaxOutputTokensを動的に増やすために使う */
    private static final int TOKENS_PER_PROBLEM = 1200;
    private static final int TOKENS_OVERHEAD = 2000;

    private static final String SYSTEM_PROMPT = """
            あなたは経験豊富なJavaメンターです。Java Silver / Gold(Oracle認定資格)の
            学習に取り組んでいるエンジニアに、指定されたレベルのテーマに沿った
            プログラミング問題を複数作成してください。

            以下の条件を守ってください。
            - 指定された「このレベルのテーマ」の範囲内の問題にする(範囲外の文法・APIを問わない)
            - 直近に出題した問題、および今回まとめて作る問題どうしでも、テーマ・内容が重複しないようにする
            - 短時間(目安15〜30分)で解ける粒度にする(長すぎる大作にしない)
            - description フィールドには、要件・入出力の例を明確に書く(Markdown形式でよい)
            - 学習者の目標(分かっていれば)を、テーマを損なわない範囲で味付けとして活かしてよい
            - title フィールドには問題の短いタイトルを、difficulty フィールドには
              「初級」「中級」「上級」のいずれかを入れてください
            - 指定された数だけ、配列で出力してください
            """;

    private static final GeminiSchema PROBLEM_SCHEMA = GeminiSchema.object(
            Map.of(
                    "title", GeminiSchema.string(),
                    "difficulty", GeminiSchema.stringEnum(List.of("初級", "中級", "上級")),
                    "description", GeminiSchema.string()
            ),
            List.of("title", "difficulty", "description")
    );

    private static final GeminiSchema RESPONSE_SCHEMA = GeminiSchema.array(PROBLEM_SCHEMA);

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.max-tokens}")
    private int baseMaxTokens;

    public GeminiProblemService(GeminiClient geminiClient, ObjectMapper objectMapper) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public record ProblemResult(String title, String difficulty, String description) {
    }

    /** 指定レベルの問題を count 問まとめて生成する */
    public List<ProblemResult> generateProblems(Level level, Goal goal, List<String> recentTitles, int count) {
        int maxTokens = Math.max(baseMaxTokens, TOKENS_OVERHEAD + TOKENS_PER_PROBLEM * count);

        GeminiRequest request = new GeminiRequest(
                List.of(GeminiContent.ofText("user", buildUserMessage(level, goal, recentTitles, count))),
                GeminiContent.ofText(null, SYSTEM_PROMPT),
                new GeminiGenerationConfig(maxTokens, RESPONSE_SCHEMA)
        );

        String text = geminiClient.generateText(request);

        try {
            List<ProblemResult> results = objectMapper.readValue(text, new TypeReference<List<ProblemResult>>() {
            });
            if (results.isEmpty()) {
                throw new IllegalStateException("Gemini APIが問題を1問も生成しませんでした。");
            }
            return results;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Gemini APIの応答を解析できませんでした: " + e.getMessage());
        }
    }

    private String buildUserMessage(Level level, Goal goal, List<String> recentTitles, int count) {
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

        sb.append("\nこのレベルの問題を").append(count).append("問作成してください。");
        return sb.toString();
    }
}
