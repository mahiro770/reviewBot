package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Gemini API のレスポンス candidates[] の1要素 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiCandidate {

    private GeminiContent content;

    public GeminiContent getContent() {
        return content;
    }

    public void setContent(GeminiContent content) {
        this.content = content;
    }
}
