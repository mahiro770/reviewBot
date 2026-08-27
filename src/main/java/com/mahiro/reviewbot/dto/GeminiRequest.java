package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Gemini API (POST /v1beta/models/{model}:generateContent) へのリクエストボディ */
public class GeminiRequest {

    private List<GeminiContent> contents;

    @JsonProperty("systemInstruction")
    private GeminiContent systemInstruction;

    @JsonProperty("generationConfig")
    private GeminiGenerationConfig generationConfig;

    public GeminiRequest() {
    }

    public GeminiRequest(List<GeminiContent> contents, GeminiContent systemInstruction, GeminiGenerationConfig generationConfig) {
        this.contents = contents;
        this.systemInstruction = systemInstruction;
        this.generationConfig = generationConfig;
    }

    public List<GeminiContent> getContents() {
        return contents;
    }

    public void setContents(List<GeminiContent> contents) {
        this.contents = contents;
    }

    public GeminiContent getSystemInstruction() {
        return systemInstruction;
    }

    public void setSystemInstruction(GeminiContent systemInstruction) {
        this.systemInstruction = systemInstruction;
    }

    public GeminiGenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setGenerationConfig(GeminiGenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }
}
