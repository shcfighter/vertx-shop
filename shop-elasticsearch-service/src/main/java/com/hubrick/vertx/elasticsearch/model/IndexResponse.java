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

@DataObject
public class IndexResponse extends AbstractResponse<IndexResponse> {

    private String index;
    private String type;
    private String id;
    private Long version;
    private Boolean created;

    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_VERSION = "version";
    public static final String JSON_FIELD_CREATED = "created";

    public IndexResponse() {
    }

    public IndexResponse(IndexResponse other) {
        super(other);

        this.index = other.getIndex();
        this.type = other.getType();
        this.id = other.getId();
        this.version = other.getVersion();
        this.created = other.getCreated();
    }

    public IndexResponse(JsonObject json) {
        super(json);

        this.index = json.getString(JSON_FIELD_INDEX);
        this.type = json.getString(JSON_FIELD_TYPE);
        this.id = json.getString(JSON_FIELD_ID);
        this.version = json.getLong(JSON_FIELD_VERSION);
        this.created = json.getBoolean(JSON_FIELD_CREATED);
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Boolean getCreated() {
        return created;
    }

    public void setCreated(Boolean created) {
        this.created = created;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (version != null) json.put(JSON_FIELD_VERSION, version);
        if (created != null) json.put(JSON_FIELD_CREATED, created);

        return json.mergeIn(super.toJson());
    }
}
