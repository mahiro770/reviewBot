package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Anthropic Messages API (POST /v1/messages) へのリクエストボディ */
public class ClaudeRequest {

    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private String system;

    private List<ClaudeMessage> messages;

    public ClaudeRequest() {
    }

    public ClaudeRequest(String model, int maxTokens, String system, List<ClaudeMessage> messages) {
        this.model = model;
        this.maxTokens = maxTokens;
        this.system = system;
        this.messages = messages;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public List<ClaudeMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ClaudeMessage> messages) {
        this.messages = messages;
    }
}
