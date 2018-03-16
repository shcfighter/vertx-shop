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
public class Suggestion {

    private String name;
    private Integer size;
    private SuggestionType suggestionType;
    private List<SuggestionEntry> entries = new LinkedList<>();

    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_SIZE = "size";
    public static final String JSON_FIELD_SUGGESTION_TYPE = "suggestionType";
    public static final String JSON_FIELD_ENTRIES = "entries";

    public Suggestion() {
    }

    public Suggestion(Suggestion other) {
        this.name = other.getName();
        this.size = other.getSize();
        this.suggestionType = other.getSuggestionType();
        this.entries = other.getEntries();
    }

    public Suggestion(JsonObject jsonObject) {
        this.name = jsonObject.getString(JSON_FIELD_NAME);
        this.size = jsonObject.getInteger(JSON_FIELD_SIZE);

        final String jsonSuggestionType = jsonObject.getString(JSON_FIELD_SUGGESTION_TYPE);
        if (jsonSuggestionType != null) {
            this.suggestionType = SuggestionType.valueOf(jsonSuggestionType);
        }

        final JsonArray jsonEntries = jsonObject.getJsonArray(JSON_FIELD_ENTRIES);
        if (jsonEntries != null) {
            for (int i = 0; i < jsonEntries.size(); i++) {
                entries.add(new SuggestionEntry(jsonEntries.getJsonObject(i)));
            }
        }
    }

    public String getName() {
        return name;
    }

    public Suggestion setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public Suggestion setSize(Integer size) {
        this.size = size;
        return this;
    }

    public SuggestionType getSuggestionType() {
        return suggestionType;
    }

    public Suggestion setSuggestionType(SuggestionType suggestionType) {
        this.suggestionType = suggestionType;
        return this;
    }

    public List<SuggestionEntry> getEntries() {
        return entries;
    }

    public Suggestion setEntries(List<SuggestionEntry> entries) {
        this.entries = entries;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (name != null) json.put(JSON_FIELD_NAME, name);
        if (size != null) json.put(JSON_FIELD_SIZE, size);
        if (suggestionType != null) json.put(JSON_FIELD_SUGGESTION_TYPE, suggestionType.name());

        if (entries != null) {
            final JsonArray jsonEntries = new JsonArray();
            entries.forEach(e -> jsonEntries.add(e.toJson()));
            json.put(JSON_FIELD_ENTRIES, jsonEntries);
        }
        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
