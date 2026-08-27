package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.dto.ClaudeMessage;
import com.mahiro.reviewbot.dto.ClaudeRequest;
import com.mahiro.reviewbot.dto.ClaudeResponse;
import com.mahiro.reviewbot.model.Goal;
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
 * Claude(Anthropic Messages API)を呼び出して、学習目標に沿った
 * 今日のプログラミング問題を1問生成するサービス。
 */
@Service
public class ClaudeProblemService {

    private static final String SYSTEM_PROMPT = """
            あなたは経験豊富なJavaメンターです。未経験からITエンジニアに転向し、
            現在Java/Spring Bootを学習中のエンジニアに、今日1問だけ解いてもらう
            プログラミング問題を作成してください。

            以下の条件を守ってください。
            - 学習者の目標・作りたいもの・1日の学習時間に合った、実践的で面白いテーマにする
            - 直近に出題した問題とテーマ・内容が重複しないようにする
            - 1日の学習時間内(目安)で解ける粒度にする(長すぎる大作にしない)
            - 問題文には、要件・入出力の例を明確に書く
            - 学習目標や1日の学習時間が指定されていない場合は、Javaの基礎力を伸ばす
              一般的な問題にする

            出力は必ず次の形式のみで、日本語で書いてください(他の文字列を前後に含めない):
            タイトル: (問題の短いタイトル)
            難易度: 初級 または 中級 または 上級
            ---
            (問題文本体。Markdown形式でよい)
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

    private static final Pattern TITLE_PATTERN = Pattern.compile("タイトル[:：]\\s*(.+)");
    private static final Pattern DIFFICULTY_PATTERN = Pattern.compile("難易度[:：]\\s*(.+)");

    public ClaudeProblemService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record ProblemResult(String title, String difficulty, String description) {
    }

    public ProblemResult generateProblem(Goal goal, List<String> recentTitles) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "ANTHROPIC_API_KEY が設定されていません。環境変数にAPIキーを設定して起動し直してください。");
        }

        ClaudeRequest request = new ClaudeRequest(
                model,
                maxTokens,
                SYSTEM_PROMPT,
                List.of(new ClaudeMessage("user", buildUserMessage(goal, recentTitles)))
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

        return parseProblem(text);
    }

    private String buildUserMessage(Goal goal, List<String> recentTitles) {
        StringBuilder sb = new StringBuilder();
        if (goal != null) {
            sb.append("## 学習目標\n");
            sb.append("目指す姿: ").append(goal.getTargetVision()).append("\n");
            if (goal.getBuildTarget() != null && !goal.getBuildTarget().isBlank()) {
                sb.append("作りたいもの: ").append(goal.getBuildTarget()).append("\n");
            }
            if (goal.getDailyMinutes() != null) {
                sb.append("1日の学習時間: ").append(goal.getDailyMinutes()).append("分\n");
            }
        } else {
            sb.append("まだ目標は設定されていません。Java全般の基礎力を伸ばす問題にしてください。\n");
        }

        if (recentTitles != null && !recentTitles.isEmpty()) {
            sb.append("\n## 直近に出題済みの問題タイトル(これらと重複しないテーマにする)\n");
            recentTitles.forEach(title -> sb.append("- ").append(title).append("\n"));
        }

        sb.append("\n今日の問題を1問作成してください。");
        return sb.toString();
    }

    private ProblemResult parseProblem(String text) {
        String[] lines = text.split("\n", -1);
        int separatorIndex = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals("---")) {
                separatorIndex = i;
                break;
            }
        }

        String header = separatorIndex >= 0
                ? String.join("\n", java.util.Arrays.asList(lines).subList(0, separatorIndex))
                : text;
        String body = separatorIndex >= 0
                ? String.join("\n", java.util.Arrays.asList(lines).subList(separatorIndex + 1, lines.length)).trim()
                : text.trim();

        Matcher titleMatcher = TITLE_PATTERN.matcher(header);
        String title = titleMatcher.find() ? titleMatcher.group(1).trim() : "今日の問題";

        Matcher difficultyMatcher = DIFFICULTY_PATTERN.matcher(header);
        String difficulty = difficultyMatcher.find() ? difficultyMatcher.group(1).trim() : null;

        if (body.isBlank()) {
            body = text.trim();
        }

        return new ProblemResult(title, difficulty, body);
    }
}
