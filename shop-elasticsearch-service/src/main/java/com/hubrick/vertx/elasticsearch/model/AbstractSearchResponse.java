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

/**
 * Search operation options
 */
public abstract class AbstractSearchResponse<T extends AbstractSearchResponse<T>> extends AbstractResponse<T> {

    private JsonObject rawResponse;

    public static final String JSON_FIELD_RAW_RESPONSE = "rawResponse";

    protected AbstractSearchResponse() {
    }

    public AbstractSearchResponse(AbstractSearchResponse other) {
        super(other);

        this.rawResponse = other.getRawResponse();
    }

    public AbstractSearchResponse(JsonObject json) {
        super(json);

        this.rawResponse = json.getJsonObject(JSON_FIELD_RAW_RESPONSE);
    }

    public JsonObject getRawResponse() {
        return rawResponse;
    }

    public T setRawResponse(JsonObject rawResponse) {
        this.rawResponse = rawResponse;
        return returnThis();
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (rawResponse != null) json.put(JSON_FIELD_RAW_RESPONSE, rawResponse);

        return json.mergeIn(super.toJson());
    }
}
