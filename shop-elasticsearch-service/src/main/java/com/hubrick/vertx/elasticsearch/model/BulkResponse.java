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

/**
 * @author sp@hubrick.com
 * @since 06.12.17
 */
@DataObject
public class BulkResponse extends AbstractRawResponse<BulkResponse> {

    public static final String JSON_FIELD_RESPONSES = "responses";
    public static final String JSON_FIELD_TOOK_IN_MILLIS = "tookInMillis";

    private List<BulkResponseItem> responses = new LinkedList<>();
    private Long tookInMillis;

    public BulkResponse() {
    }

    public BulkResponse(JsonObject json) {
        super(json);
        final JsonArray jsonResponses = json.getJsonArray(JSON_FIELD_RESPONSES, new JsonArray());
        for (int i = 0; i < jsonResponses.size(); i++) {
            this.responses.add(new BulkResponseItem(jsonResponses.getJsonObject(i)));
        }
        this.tookInMillis = json.getLong(JSON_FIELD_TOOK_IN_MILLIS);
    }

    public List<BulkResponseItem> getResponses() {
        return responses;
    }

    public void setResponses(final List<BulkResponseItem> responses) {
        this.responses = responses;
    }

    public Long getTookInMillis() {
        return tookInMillis;
    }

    public void setTookInMillis(final Long tookInMillis) {
        this.tookInMillis = tookInMillis;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (responses != null) {
            final JsonArray jsonResponses = new JsonArray();
            responses.forEach(e -> jsonResponses.add(e.toJson()));
            json.put(JSON_FIELD_RESPONSES, jsonResponses);
        }
        if (tookInMillis != null) json.put(JSON_FIELD_TOOK_IN_MILLIS, tookInMillis);

        return json.mergeIn(super.toJson());
    }

}
