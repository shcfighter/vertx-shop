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
package com.ecit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class Hit {

    @JsonProperty("_index")
    private String index;
    @JsonProperty("_type")
    private String type;
    @JsonProperty("_id")
    private String id;
    @JsonProperty("_score")
    private Float score;
    private List<Long> sort;
    @JsonProperty("_source")

    private Map<String, Object> source = new HashMap<>();
    private Map<String, List<Object>> fields = new HashMap<>();

    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_SCORE = "score";
    public static final String JSON_FIELD_SORT = "sort";
    public static final String JSON_FIELD_SOURCE = "source";
    public static final String JSON_FIELD_FIELDS = "fields";

    public Hit() {
    }

    public Hit(Hit other) {
        this.index = other.getIndex();
        this.type = other.getType();
        this.id = other.getId();
        this.score = other.getScore();
        this.sort = other.getSort();
        this.source = other.getSource();
        this.fields = other.getFields();

    }

    public Hit(JsonObject jsonObject) {
        this.index = jsonObject.getString(JSON_FIELD_INDEX);
        this.type = jsonObject.getString(JSON_FIELD_TYPE);
        this.id = jsonObject.getString(JSON_FIELD_ID);
        this.score = jsonObject.getFloat(JSON_FIELD_SCORE);
        this.sort = Objects.isNull(jsonObject.getJsonArray(JSON_FIELD_SORT)) ? Lists.newArrayList() : jsonObject.getJsonArray(JSON_FIELD_SORT).getList();
        this.source = jsonObject.getJsonObject(JSON_FIELD_SOURCE).getMap();

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

    public Hit setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public Hit setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public Hit setId(String id) {
        this.id = id;
        return this;
    }

    public Float getScore() {
        return score;
    }

    public Hit setScore(Float score) {
        this.score = score;
        return this;
    }

    public List<Long> getSort() {
        return sort;
    }

    public void setSort(List<Long> sort) {
        this.sort = sort;
    }

    public Map<String, Object> getSource() {
        return source;
    }

    public Hit setSource(Map<String, Object> source) {
        this.source = source;
        return this;
    }

    public Map<String, List<Object>> getFields() {
        return fields;
    }

    public Hit setFields(Map<String, List<Object>> fields) {
        this.fields = fields;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (score != null) json.put(JSON_FIELD_SCORE, score);
        if (sort != null) json.put(JSON_FIELD_SORT, sort);
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

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
