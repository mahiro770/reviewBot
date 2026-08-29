package com.mahiro.reviewbot.service;

import com.mahiro.reviewbot.dto.GeminiContent;
import com.mahiro.reviewbot.dto.GeminiRequest;
import com.mahiro.reviewbot.dto.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Gemini(Google Generative Language API)への実際のHTTP呼び出しをまとめるクライアント。
 * GeminiReviewService/GeminiProblemService の両方から使われる共通処理
 * (APIキーチェック・URL組み立て・一時的なエラーのリトライ)をここに集約している。
 */
@Service
public class GeminiClient {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_BASE_DELAY_MS = 500;

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base-url}")
    private String baseUrl;

    @Value("${gemini.api.model}")
    private String model;

    public GeminiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /** 構造化出力のテキスト部分(JSON文字列)を返す。429/5xx/通信エラーは指数バックオフで数回まで自動リトライする */
    public String generateText(GeminiRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY が設定されていません。環境変数にAPIキーを設定して起動し直してください。");
        }

        String url = baseUrl + "/" + model + ":generateContent";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        RuntimeException lastError = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                GeminiResponse response = restTemplate.postForObject(url, entity, GeminiResponse.class);
                String text = extractText(response);
                if (text.isBlank()) {
                    throw new IllegalStateException("Gemini APIから空のレスポンスが返ってきました。");
                }
                return text;
            } catch (HttpStatusCodeException e) {
                if (!isRetryable(e.getStatusCode()) || attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException("Gemini APIの呼び出しに失敗しました(" + e.getStatusCode() + ")", e);
                }
                lastError = e;
            } catch (ResourceAccessException e) {
                if (attempt == MAX_ATTEMPTS) {
                    throw new IllegalStateException("Gemini APIへの接続に失敗しました: " + e.getMessage(), e);
                }
                lastError = e;
            }
            sleep(RETRY_BASE_DELAY_MS * (1L << (attempt - 1)));
        }
        throw lastError != null ? lastError : new IllegalStateException("Gemini APIの呼び出しに失敗しました");
    }

    private boolean isRetryable(HttpStatusCode status) {
        return status.value() == 429 || status.is5xxServerError();
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
}
