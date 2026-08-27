package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Gemini API (generateContent) のレスポンスボディ(使うフィールドだけ抜粋) */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {

    private List<GeminiCandidate> candidates;

    public List<GeminiCandidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<GeminiCandidate> candidates) {
        this.candidates = candidates;
    }
}
