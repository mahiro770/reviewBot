package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Gemini API の generationConfig */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiGenerationConfig {

    @JsonProperty("maxOutputTokens")
    private int maxOutputTokens;

    @JsonProperty("responseMimeType")
    private String responseMimeType;

    @JsonProperty("responseSchema")
    private GeminiSchema responseSchema;

    public GeminiGenerationConfig() {
    }

    public GeminiGenerationConfig(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    /** 構造化出力(JSON)を強制する場合に使うコンストラクタ */
    public GeminiGenerationConfig(int maxOutputTokens, GeminiSchema responseSchema) {
        this.maxOutputTokens = maxOutputTokens;
        this.responseMimeType = "application/json";
        this.responseSchema = responseSchema;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public String getResponseMimeType() {
        return responseMimeType;
    }

    public void setResponseMimeType(String responseMimeType) {
        this.responseMimeType = responseMimeType;
    }

    public GeminiSchema getResponseSchema() {
        return responseSchema;
    }

    public void setResponseSchema(GeminiSchema responseSchema) {
        this.responseSchema = responseSchema;
    }
}
