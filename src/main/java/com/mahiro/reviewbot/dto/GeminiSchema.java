package com.mahiro.reviewbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Gemini API の構造化出力(responseSchema)に渡すJSON Schema(のサブセット)。
 * type は Gemini独自の大文字表記("OBJECT","STRING","INTEGER","BOOLEAN"等)を使う。
 * これを使うことで、レビュー結果や生成した問題を自由記述+正規表現抽出ではなく
 * 型付きJSONとして確実に受け取れる。
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiSchema {

    private String type;
    private Map<String, GeminiSchema> properties;
    private List<String> required;
    private GeminiSchema items;

    @JsonProperty("enum")
    private List<String> enumValues;

    public static GeminiSchema string() {
        GeminiSchema schema = new GeminiSchema();
        schema.type = "STRING";
        return schema;
    }

    public static GeminiSchema stringEnum(List<String> values) {
        GeminiSchema schema = string();
        schema.enumValues = values;
        return schema;
    }

    public static GeminiSchema integer() {
        GeminiSchema schema = new GeminiSchema();
        schema.type = "INTEGER";
        return schema;
    }

    public static GeminiSchema bool() {
        GeminiSchema schema = new GeminiSchema();
        schema.type = "BOOLEAN";
        return schema;
    }

    public static GeminiSchema object(Map<String, GeminiSchema> properties, List<String> required) {
        GeminiSchema schema = new GeminiSchema();
        schema.type = "OBJECT";
        schema.properties = properties;
        schema.required = required;
        return schema;
    }

    public static GeminiSchema array(GeminiSchema itemSchema) {
        GeminiSchema schema = new GeminiSchema();
        schema.type = "ARRAY";
        schema.items = itemSchema;
        return schema;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, GeminiSchema> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, GeminiSchema> properties) {
        this.properties = properties;
    }

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<String> enumValues) {
        this.enumValues = enumValues;
    }

    public GeminiSchema getItems() {
        return items;
    }

    public void setItems(GeminiSchema items) {
        this.items = items;
    }
}
