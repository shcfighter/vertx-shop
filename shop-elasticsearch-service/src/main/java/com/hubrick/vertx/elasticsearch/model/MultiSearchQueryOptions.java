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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Multisearch operation query options
 *
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class MultiSearchQueryOptions {

    private static final String JSON_FIELD_INDICES = "indices";
    private static final String JSON_FIELD_SEARCH_OPTIONS = "searchOptions";

    private List<String> indices = new LinkedList<>();
    private SearchOptions searchOptions;

    public MultiSearchQueryOptions() {
    }

    public MultiSearchQueryOptions(MultiSearchQueryOptions other) {
        this.indices = other.getIndices();
        this.searchOptions = other.getSearchOptions();
    }

    public MultiSearchQueryOptions(JsonObject json) {
        final JsonArray indicesJsonArray = Optional.ofNullable(json.getJsonArray(JSON_FIELD_INDICES)).orElse(new JsonArray());
        for (int i = 0; i < indicesJsonArray.size(); i++) {
            indices.add(indicesJsonArray.getString(i));
        }
        this.searchOptions = Optional.ofNullable(json.getJsonObject(JSON_FIELD_SEARCH_OPTIONS)).map(SearchOptions::new).orElse(null);
    }

    public List<String> getIndices() {
        return indices;
    }

    public MultiSearchQueryOptions setIndices(List<String> indices) {
        this.indices = indices;
        return this;
    }

    @GenIgnore
    public MultiSearchQueryOptions addIndex(String index) {
        this.indices.add(index);
        return this;
    }

    public SearchOptions getSearchOptions() {
        return searchOptions;
    }

    public MultiSearchQueryOptions setSearchOptions(SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (indices != null) json.put(JSON_FIELD_INDICES, new JsonArray(indices));
        if (searchOptions != null) json.put(JSON_FIELD_SEARCH_OPTIONS, searchOptions.toJson());

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
