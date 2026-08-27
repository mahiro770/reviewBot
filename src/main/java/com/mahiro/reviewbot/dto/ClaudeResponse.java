package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Anthropic Messages APIのレスポンスボディ(使うフィールドだけ抜粋) */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResponse {

    private List<ClaudeContentBlock> content;

    public List<ClaudeContentBlock> getContent() {
        return content;
    }

    public void setContent(List<ClaudeContentBlock> content) {
        this.content = content;
    }
}
