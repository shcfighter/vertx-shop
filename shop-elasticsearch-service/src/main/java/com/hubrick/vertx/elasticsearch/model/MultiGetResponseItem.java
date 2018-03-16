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
public class MultiGetResponseItem {

    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_GET_RESULT = "getResult";
    public static final String JSON_FIELD_FAILURE_MESSAGE = "failureMessage";

    private String index;
    private String type;
    private String id;
    private GetResult getResult;
    private String failureMessage;

    public MultiGetResponseItem() {
    }

    public MultiGetResponseItem(JsonObject json) {
        this.index = json.getString(JSON_FIELD_INDEX);
        this.type = json.getString(JSON_FIELD_TYPE);
        this.id = json.getString(JSON_FIELD_ID);
        this.getResult = Optional.ofNullable(json.getJsonObject(JSON_FIELD_GET_RESULT)).map(GetResult::new).orElse(null);
        this.failureMessage = json.getString(JSON_FIELD_FAILURE_MESSAGE);
    }

    public String getIndex() {
        return index;
    }

    public MultiGetResponseItem setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public MultiGetResponseItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public MultiGetResponseItem setId(String id) {
        this.id = id;
        return this;
    }

    public GetResult getGetResult() {
        return getResult;
    }

    public MultiGetResponseItem setGetResult(GetResult getResult) {
        this.getResult = getResult;
        return this;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public MultiGetResponseItem setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (getResult != null) json.put(JSON_FIELD_GET_RESULT, getResult.toJson());
        if (failureMessage != null) json.put(JSON_FIELD_FAILURE_MESSAGE, failureMessage);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
