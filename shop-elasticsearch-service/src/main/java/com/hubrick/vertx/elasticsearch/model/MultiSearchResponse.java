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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class MultiSearchResponse extends AbstractRawResponse<MultiSearchResponse> {

    public static final String JSON_FIELD_RESPONSES = "responses";

    private List<MultiSearchResponseItem> responses = new LinkedList<>();

    public MultiSearchResponse() {
    }

    public MultiSearchResponse(JsonObject json) {
        super(json);
        final JsonArray jsonResponses = json.getJsonArray(JSON_FIELD_RESPONSES, new JsonArray());
        for (int i = 0; i < jsonResponses.size(); i++) {
            this.responses.add(new MultiSearchResponseItem(jsonResponses.getJsonObject(i)));
        }
    }

    public List<MultiSearchResponseItem> getResponses() {
        return responses;
    }

    public MultiSearchResponse setResponses(final List<MultiSearchResponseItem> responses) {
        this.responses = responses;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (!responses.isEmpty()) json.put(JSON_FIELD_RESPONSES, new JsonArray(responses.stream().map(MultiSearchResponseItem::toJson).collect(Collectors.toList())));

        return json.mergeIn(super.toJson());
    }

}
