package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Claudeのレスポンス content 配列の1要素 (type: "text" のブロックだけ使う) */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeContentBlock {

    private String type;
    private String text;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
