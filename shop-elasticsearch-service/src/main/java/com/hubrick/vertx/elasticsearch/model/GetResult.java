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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class GetResult {

    private String index;
    private String type;
    private String id;
    private Long version;
    private Boolean exists;
    private JsonObject source;
    private Map<String, List<Object>> fields = new HashMap<>();

    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_VERSION = "version";
    public static final String JSON_FIELD_EXISTS = "exists";
    public static final String JSON_FIELD_SOURCE = "source";
    public static final String JSON_FIELD_FIELDS = "fields";

    public GetResult() {
    }

    public GetResult(GetResult other) {
        this.index = other.getIndex();
        this.type = other.getType();
        this.id = other.getId();
        this.version = other.getVersion();
        this.exists = other.getExists();
        this.source = other.getSource();
        this.fields = other.getFields();

    }

    public GetResult(JsonObject jsonObject) {
        this.index = jsonObject.getString(JSON_FIELD_INDEX);
        this.type = jsonObject.getString(JSON_FIELD_TYPE);
        this.id = jsonObject.getString(JSON_FIELD_ID);
        this.version = jsonObject.getLong(JSON_FIELD_VERSION);
        this.exists = jsonObject.getBoolean(JSON_FIELD_EXISTS);
        this.source = jsonObject.getJsonObject(JSON_FIELD_SOURCE);

        final JsonObject jsonFields = jsonObject.getJsonObject(JSON_FIELD_FIELDS);
        if (jsonFields != null) {
            for (String fieldName : jsonFields.fieldNames()) {
                final List<Object> fieldValues = new LinkedList<>();
                jsonFields.getJsonArray(fieldName).stream().forEach(e -> fieldValues.add(e));
                this.fields.put(fieldName, fieldValues);
            }
        }
    }

    public String getIndex() {
        return index;
    }

    public GetResult setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public GetResult setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public GetResult setId(String id) {
        this.id = id;
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public GetResult setVersion(Long version) {
        this.version = version;
        return this;
    }

    public Boolean getExists() {
        return exists;
    }

    public GetResult setExists(Boolean exists) {
        this.exists = exists;
        return this;
    }

    public JsonObject getSource() {
        return source;
    }

    public GetResult setSource(JsonObject source) {
        this.source = source;
        return this;
    }

    public Map<String, List<Object>> getFields() {
        return fields;
    }

    public GetResult setFields(Map<String, List<Object>> fields) {
        this.fields = fields;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (version != null) json.put(JSON_FIELD_VERSION, version);
        if (exists != null) json.put(JSON_FIELD_EXISTS, exists);
        if (source != null) json.put(JSON_FIELD_SOURCE, source);

        if (!fields.isEmpty()) {
            final JsonObject jsonFields = new JsonObject();
            fields.entrySet().forEach(entry -> {
                final JsonArray jsonFieldValues = new JsonArray();
                entry.getValue().forEach(jsonFieldValues::add);
                jsonFields.put(entry.getKey(), jsonFieldValues);
            });
            json.put(JSON_FIELD_FIELDS, jsonFields);
        }

        return json;
    }
}
