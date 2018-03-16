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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class SuggestionEntry {

    private String text;
    private Integer offset;
    private Integer length;
    private List<SuggestionEntryOption> options = new LinkedList<>();

    public static final String JSON_FIELD_TEXT = "text";
    public static final String JSON_FIELD_OFFSET = "offset";
    public static final String JSON_FIELD_LENGTH = "length";
    public static final String JSON_FIELD_OPTIONS = "options";

    public SuggestionEntry() {
    }

    public SuggestionEntry(SuggestionEntry other) {
        this.text = other.getText();
        this.offset = other.getOffset();
        this.length = other.getLength();
        this.options = other.getOptions();
    }

    public SuggestionEntry(JsonObject jsonObject) {
        this.text = jsonObject.getString(JSON_FIELD_TEXT);
        this.offset = jsonObject.getInteger(JSON_FIELD_OFFSET);
        this.length = jsonObject.getInteger(JSON_FIELD_LENGTH);

        final JsonArray jsonOptions = jsonObject.getJsonArray(JSON_FIELD_OPTIONS);
        if (jsonOptions != null) {
            for (int i = 0; i < jsonOptions.size(); i++) {
                options.add(new SuggestionEntryOption(jsonOptions.getJsonObject(i)));
            }
        }
    }

    public String getText() {
        return text;
    }

    public SuggestionEntry setText(String text) {
        this.text = text;
        return this;
    }

    public Integer getOffset() {
        return offset;
    }

    public SuggestionEntry setOffset(Integer offset) {
        this.offset = offset;
        return this;
    }

    public Integer getLength() {
        return length;
    }

    public SuggestionEntry setLength(Integer length) {
        this.length = length;
        return this;
    }

    public List<SuggestionEntryOption> getOptions() {
        return options;
    }

    public SuggestionEntry setOptions(List<SuggestionEntryOption> options) {
        this.options = options;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (text != null) json.put(JSON_FIELD_TEXT, text);
        if (offset != null) json.put(JSON_FIELD_OFFSET, offset);
        if (length != null) json.put(JSON_FIELD_LENGTH, length);

        if (!options.isEmpty()) {
            final JsonArray jsonOptions = new JsonArray();
            options.forEach(e -> jsonOptions.add(e.toJson()));
            json.put(JSON_FIELD_OPTIONS, jsonOptions);
        }

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
