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

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public abstract class BaseSuggestOption {

    private SuggestionType suggestionType;

    public static final String JSON_FIELD_SUGGESTION_TYPE = "type";

    protected BaseSuggestOption(SuggestionType suggestionType) {
        this.suggestionType = suggestionType;
    }

    public BaseSuggestOption(BaseSuggestOption other) {
        suggestionType = other.getSuggestionType();
    }

    public BaseSuggestOption(JsonObject json) {
        try {
            suggestionType = SuggestionType.valueOf(json.getString(JSON_FIELD_SUGGESTION_TYPE));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Type " + json.getString(JSON_FIELD_SUGGESTION_TYPE) + " is not supported");
        }
    }

    public SuggestionType getSuggestionType() {
        return suggestionType;
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put(JSON_FIELD_SUGGESTION_TYPE, suggestionType.name());
    }

    public static BaseSuggestOption parseJson(JsonObject jsonObject) {
        try {
            final SuggestionType suggestionType = SuggestionType.valueOf(jsonObject.getString(JSON_FIELD_SUGGESTION_TYPE));
            switch (suggestionType) {
                case COMPLETION:
                    return new CompletionSuggestOption(jsonObject);
                default:
                    throw new IllegalArgumentException("SuggestType " + jsonObject.getString(JSON_FIELD_SUGGESTION_TYPE) + " is not supported");
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("SuggestType " + jsonObject.getString(JSON_FIELD_SUGGESTION_TYPE) + " is not supported");
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
