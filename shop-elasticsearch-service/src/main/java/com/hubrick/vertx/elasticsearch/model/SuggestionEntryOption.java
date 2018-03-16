/**
 * Copyright (C) 2016 Etaia AS (oss@hubrick.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubrick.vertx.elasticsearch.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class SuggestionEntryOption {

    private String text;
    private String highlight;
    private Float score;

    private static final String JSON_FIELD_TEXT = "text";
    private static final String JSON_FIELD_HIGHLIGHT = "highlight";
    private static final String JSON_FIELD_SCORE = "score";

    public SuggestionEntryOption() {
    }

    public SuggestionEntryOption(SuggestionEntryOption other) {
        this.text = other.getText();
        this.highlight = other.getHighlight();
        this.score = other.getScore();
    }

    public SuggestionEntryOption(JsonObject jsonObject) {
        this.text = jsonObject.getString(JSON_FIELD_TEXT);
        this.highlight = jsonObject.getString(JSON_FIELD_HIGHLIGHT);
        this.score = jsonObject.getFloat(JSON_FIELD_SCORE);
    }

    public String getText() {
        return text;
    }

    public SuggestionEntryOption setText(String text) {
        this.text = text;
        return this;
    }

    public String getHighlight() {
        return highlight;
    }

    public SuggestionEntryOption setHighlight(String highlight) {
        this.highlight = highlight;
        return this;
    }

    public Float getScore() {
        return score;
    }

    public SuggestionEntryOption setScore(Float score) {
        this.score = score;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (text != null) json.put(JSON_FIELD_TEXT, text);
        if (highlight != null) json.put(JSON_FIELD_HIGHLIGHT, highlight);
        if (score != null) json.put(JSON_FIELD_SCORE, score);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
