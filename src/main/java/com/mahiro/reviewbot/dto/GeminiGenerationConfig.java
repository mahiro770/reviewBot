package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Gemini API の generationConfig */
public class GeminiGenerationConfig {

    @JsonProperty("maxOutputTokens")
    private int maxOutputTokens;

    public GeminiGenerationConfig() {
    }

    public GeminiGenerationConfig(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public int getMaxOutputTokens() {
        return maxOutputTokens;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }
}
