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

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class CompletionSuggestOption extends BaseSuggestOption {

    public static final String FIELD_SUGGESTION_TEXT = "text";
    public static final String FIELD_SUGGESTION_FIELD = "field";
    public static final String FIELD_SUGGESTION_SIZE = "size";

    private String text;
    private String field;
    private Integer size;

    public CompletionSuggestOption() {
        super(SuggestionType.COMPLETION);
    }

    public CompletionSuggestOption(CompletionSuggestOption other) {
        super(other);

        text = other.getText();
        field = other.getField();
        size = other.getSize();
    }

    public CompletionSuggestOption(JsonObject json) {
        super(json);

        text = json.getString(FIELD_SUGGESTION_TEXT);
        field = json.getString(FIELD_SUGGESTION_FIELD);
        size = json.getInteger(FIELD_SUGGESTION_SIZE);
    }

    public String getText() {
        return text;
    }

    public CompletionSuggestOption setText(final String text) {
        this.text = text;
        return this;
    }

    public String getField() {
        return field;
    }

    public CompletionSuggestOption setField(final String field) {
        this.field = field;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public CompletionSuggestOption setSize(final Integer size) {
        this.size = size;
        return this;
    }

    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (getText() != null) json.put(FIELD_SUGGESTION_TEXT,getText());
        if (getField() != null) json.put(FIELD_SUGGESTION_FIELD,getField());
        if (getSize() != null) json.put(FIELD_SUGGESTION_SIZE,getSize());

        return json.mergeIn(super.toJson());
    }
}
