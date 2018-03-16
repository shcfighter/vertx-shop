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
 * @author sp@hubrick.com
 * @since 06.12.17
 */
@DataObject
public class BulkResponseItem {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_SHARDS = "shards";
    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_OP_TYPE = "opType";
    public static final String JSON_FIELD_FAILURE = "failure";
    public static final String JSON_FIELD_FAILURE_MESSAGE = "failureMessage";

    private String id;
    private Shards shards;
    private String index;
    private String type;
    private OpType opType;
    private JsonObject failure = new JsonObject();
    private String failureMessage;

    public BulkResponseItem() {
    }

    public BulkResponseItem(JsonObject json) {
        this.id = json.getString(JSON_FIELD_ID);
        this.shards = new Shards(json.getJsonObject(JSON_FIELD_SHARDS));
        this.index = json.getString(JSON_FIELD_INDEX);
        this.type = json.getString(JSON_FIELD_TYPE);
        this.opType = Optional.ofNullable(json.getString(JSON_FIELD_OP_TYPE)).map(OpType::valueOf).orElse(null);
        this.failure = json.getJsonObject(JSON_FIELD_FAILURE);
        this.failureMessage = json.getString(JSON_FIELD_FAILURE_MESSAGE);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Shards getShards() {
        return shards;
    }

    public void setShards(final Shards shards) {
        this.shards = shards;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OpType getOpType() {
        return opType;
    }

    public void setOpType(OpType opType) {
        this.opType = opType;
    }

    public JsonObject getFailure() {
        return failure;
    }

    public void setFailure(JsonObject failure) {
        this.failure = failure;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (id != null) json.put(JSON_FIELD_ID, id);
        if (shards != null) json.put(JSON_FIELD_SHARDS, shards.toJson());
        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (opType != null) json.put(JSON_FIELD_OP_TYPE, opType.name());
        if (failure != null) json.put(JSON_FIELD_FAILURE, failure);
        if (failureMessage != null) json.put(JSON_FIELD_FAILURE_MESSAGE, failureMessage);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
