package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Gemini API の contents[].parts[] / candidates[].content.parts[] の1要素 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiPart {

    private String text;

    public GeminiPart() {
    }

    public GeminiPart(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
