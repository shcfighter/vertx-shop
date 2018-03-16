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
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class MultiSearchResponseItem {

    public static final String JSON_FIELD_RESPONSE = "response";
    public static final String JSON_FIELD_FAILURE_MESSAGE = "failureMessage";

    private SearchResponse searchResponse;
    private String failureMessage;

    public MultiSearchResponseItem() {
    }

    public MultiSearchResponseItem(JsonObject json) {
        this.searchResponse = Optional.ofNullable(json.getJsonObject(JSON_FIELD_RESPONSE)).map(SearchResponse::new).orElse(null);
        this.failureMessage = json.getString(JSON_FIELD_FAILURE_MESSAGE);
    }

    public SearchResponse getSearchResponse() {
        return searchResponse;
    }

    public MultiSearchResponseItem setSearchResponse(SearchResponse searchResponse) {
        this.searchResponse = searchResponse;
        return this;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public MultiSearchResponseItem setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (searchResponse != null) json.put(JSON_FIELD_RESPONSE, searchResponse.toJson());
        if (failureMessage != null) json.put(JSON_FIELD_FAILURE_MESSAGE, failureMessage);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
