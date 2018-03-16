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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Search operation options
 */
@DataObject
public class SearchResponse extends AbstractSearchResponse<SearchResponse> {

    private Long took;
    private Boolean timedOut;
    private Hits hits;
    private String scrollId;
    private Map<String, JsonObject> aggregations = new HashMap<>();
    private Map<String, Suggestion> suggestions = new HashMap<>();

    public static final String JSON_FIELD_TOOK = "took";
    public static final String JSON_FIELD_TIMEOUT = "timedOut";
    public static final String JSON_FIELD_HITS = "hits";
    public static final String JSON_FIELD_SCROLL_ID = "scrollId";
    public static final String JSON_FIELD_AGGREGATIONS = "aggregations";
    public static final String JSON_FIELD_SUGGESTION = "suggestions";

    public SearchResponse() {
    }

    public SearchResponse(SearchResponse other) {
        super(other);

        this.took = other.getTook();
        this.timedOut = other.getTimedOut();
        this.hits = other.getHits();
        this.scrollId = other.getScrollId();
        this.aggregations = other.getAggregations();
        this.suggestions = other.getSuggestions();
    }

    public SearchResponse(JsonObject json) {
        super(json);

        this.took = json.getLong(JSON_FIELD_TOOK);
        this.timedOut = json.getBoolean(JSON_FIELD_TIMEOUT);

        final JsonObject jsonHits = json.getJsonObject(JSON_FIELD_HITS);
        if (jsonHits != null) {
            this.hits = new Hits(jsonHits);
        }

        this.scrollId = json.getString(JSON_FIELD_SCROLL_ID);

        final JsonObject jsonSuggestions = json.getJsonObject(JSON_FIELD_SUGGESTION);
        if (jsonSuggestions != null) {
            for (String name : jsonSuggestions.fieldNames()) {
                this.suggestions.put(name, new Suggestion(jsonSuggestions.getJsonObject(name)));
            }
        }

        final JsonObject jsonAggregations = json.getJsonObject(JSON_FIELD_AGGREGATIONS);
        if (jsonAggregations != null) {
            for (String name : jsonAggregations.fieldNames()) {
                aggregations.put(name, jsonAggregations.getJsonObject(name));
            }
        }
    }

    public Long getTook() {
        return took;
    }

    public SearchResponse setTook(Long took) {
        this.took = took;
        return this;
    }

    public Boolean getTimedOut() {
        return timedOut;
    }

    public SearchResponse setTimedOut(Boolean timedOut) {
        this.timedOut = timedOut;
        return this;
    }

    public Hits getHits() {
        return hits;
    }

    public SearchResponse setHits(Hits hits) {
        this.hits = hits;
        return this;
    }

    public String getScrollId() {
        return scrollId;
    }

    public SearchResponse setScrollId(String scrollId) {
        this.scrollId = scrollId;
        return this;
    }

    public Map<String, JsonObject> getAggregations() {
        return aggregations;
    }

    public SearchResponse setAggregations(Map<String, JsonObject> aggregations) {
        this.aggregations = aggregations;
        return this;
    }

    @GenIgnore
    public SearchResponse addSuggestion(String name, Suggestion suggestion) {
        suggestions.put(name, suggestion);
        return this;
    }

    public Map<String, Suggestion> getSuggestions() {
        return suggestions;
    }

    public SearchResponse setSuggestions(Map<String, Suggestion> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (took != null) json.put(JSON_FIELD_TOOK, took);
        if (timedOut != null) json.put(JSON_FIELD_TIMEOUT, timedOut);
        if (hits != null) json.put(JSON_FIELD_HITS, hits.toJson());
        if (scrollId != null) json.put(JSON_FIELD_SCROLL_ID, scrollId);

        if (suggestions != null) {
            final JsonObject jsonSuggestions = new JsonObject();
            suggestions.entrySet().forEach(e -> jsonSuggestions.put(e.getKey(), e.getValue().toJson()));
            json.put(JSON_FIELD_SUGGESTION, jsonSuggestions);
        }

        if (aggregations != null) {
            final JsonObject jsonAggregations = new JsonObject();
            aggregations.entrySet().forEach(e -> jsonAggregations.put(e.getKey(), e.getValue()));
            json.put(JSON_FIELD_AGGREGATIONS, jsonAggregations);
        }

        return json.mergeIn(super.toJson());
    }
}
