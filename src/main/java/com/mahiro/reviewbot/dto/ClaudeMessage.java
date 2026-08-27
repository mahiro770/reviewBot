package com.mahiro.reviewbot.dto;

/** Claude Messages APIの messages 配列の1要素 */
public class ClaudeMessage {
    private String role;
    private String content;

    public ClaudeMessage() {
    }

    public ClaudeMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
