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

import java.util.Optional;

/**
 * Multisearch operation options
 *
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class MultiSearchOptions  {

    private static final String JSON_FIELD_INDICES_OPTIONS = "indicesOptions";
    private static final String JSON_FIELD_MAX_CONCURRENT_SEARCH_REQUESTS = "maxConcurrentSearchRequests";

    private IndicesOptions indicesOptions;
    private Integer maxConcurrentSearchRequests;

    public MultiSearchOptions() {
    }

    public MultiSearchOptions(MultiSearchOptions other) {
        this.indicesOptions = other.getIndicesOptions();
        this.maxConcurrentSearchRequests = other.getMaxConcurrentSearchRequests();
    }

    public MultiSearchOptions(JsonObject json) {
        this.indicesOptions = Optional.ofNullable(json.getJsonObject(JSON_FIELD_INDICES_OPTIONS)).map(IndicesOptions::new).orElse(null);
        this.maxConcurrentSearchRequests = json.getInteger(JSON_FIELD_MAX_CONCURRENT_SEARCH_REQUESTS);
    }

    public IndicesOptions getIndicesOptions() {
        return indicesOptions;
    }

    public MultiSearchOptions setIndicesOptions(IndicesOptions indicesOptions) {
        this.indicesOptions = indicesOptions;
        return this;
    }

    public Integer getMaxConcurrentSearchRequests() {
        return maxConcurrentSearchRequests;
    }

    public MultiSearchOptions setMaxConcurrentSearchRequests(Integer maxConcurrentSearchRequests) {
        this.maxConcurrentSearchRequests = maxConcurrentSearchRequests;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (indicesOptions != null) json.put(JSON_FIELD_INDICES_OPTIONS, indicesOptions.toJson());
        if (maxConcurrentSearchRequests != null) json.put(JSON_FIELD_MAX_CONCURRENT_SEARCH_REQUESTS, maxConcurrentSearchRequests);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
