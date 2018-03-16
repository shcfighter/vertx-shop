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
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;

/**
 * MultiGet operation query options
 *
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class MultiGetQueryOptions {

    private static final String JSON_FIELD_INDEX = "index";
    private static final String JSON_FIELD_TYPE = "type";
    private static final String JSON_FIELD_ID = "id";
    private static final String JSON_FIELD_ROUTING = "routing";
    private static final String JSON_FIELD_PARENT = "parent";
    private static final String JSON_FIELD_STORED_FIELDS = "storedFields";
    private static final String JSON_FIELD_FETCH_SOURCE = "fetchSource";
    private static final String JSON_FIELD_FETCH_SOURCE_INCLUDES = "fetchSourceIncludes";
    private static final String JSON_FIELD_FETCH_SOURCE_EXCLUDES = "fetchSourceExcludes";

    private String index;
    private String type;
    private String id;
    private String routing;
    private String parent;
    private List<String> storedFields = new LinkedList<>();
    private Boolean fetchSource;
    private List<String> fetchSourceIncludes = new LinkedList<>();
    private List<String> fetchSourceExcludes = new LinkedList<>();

    public MultiGetQueryOptions() {
    }

    public MultiGetQueryOptions(MultiGetQueryOptions other) {
        this.index = other.getIndex();
        this.type = other.getType();
        this.id = other.getId();
        this.routing = other.getRouting();
        this.parent = other.getParent();
        this.storedFields = other.getStoredFields();
        this.fetchSource = other.getFetchSource();
        this.fetchSourceIncludes = other.getFetchSourceIncludes();
        this.fetchSourceExcludes = other.getFetchSourceExcludes();
    }

    public MultiGetQueryOptions(JsonObject json) {
        this.index = json.getString(JSON_FIELD_INDEX);
        this.type = json.getString(JSON_FIELD_TYPE);
        this.id = json.getString(JSON_FIELD_ID);
        this.routing = json.getString(JSON_FIELD_ROUTING);
        this.parent = json.getString(JSON_FIELD_PARENT);

        final JsonArray storedFieldsJsonArray = json.getJsonArray(JSON_FIELD_STORED_FIELDS, new JsonArray());
        for (int i = 0; i < storedFieldsJsonArray.size(); i++) {
            this.storedFields.add(storedFieldsJsonArray.getString(i));
        }

        this.fetchSource = json.getBoolean(JSON_FIELD_FETCH_SOURCE);

        final JsonArray fetchSourceIncludesJsonArray = json.getJsonArray(JSON_FIELD_FETCH_SOURCE_INCLUDES, new JsonArray());
        for (int i = 0; i < fetchSourceIncludesJsonArray.size(); i++) {
            this.fetchSourceIncludes.add(fetchSourceIncludesJsonArray.getString(i));
        }

        final JsonArray fetchSourceExcludesJsonArray = json.getJsonArray(JSON_FIELD_FETCH_SOURCE_EXCLUDES, new JsonArray());
        for (int i = 0; i < fetchSourceExcludesJsonArray.size(); i++) {
            this.fetchSourceExcludes.add(fetchSourceExcludesJsonArray.getString(i));
        }
    }

    public String getIndex() {
        return index;
    }

    public MultiGetQueryOptions setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public MultiGetQueryOptions setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public MultiGetQueryOptions setId(String id) {
        this.id = id;
        return this;
    }

    public String getRouting() {
        return routing;
    }

    public MultiGetQueryOptions setRouting(String routing) {
        this.routing = routing;
        return this;
    }

    public String getParent() {
        return parent;
    }

    public MultiGetQueryOptions setParent(String parent) {
        this.parent = parent;
        return this;
    }

    public List<String> getStoredFields() {
        return storedFields;
    }

    public MultiGetQueryOptions setStoredFields(List<String> storedFields) {
        this.storedFields = storedFields;
        return this;
    }

    public Boolean getFetchSource() {
        return fetchSource;
    }

    public MultiGetQueryOptions setFetchSource(Boolean fetchSource) {
        this.fetchSource = fetchSource;
        return this;
    }

    public List<String> getFetchSourceIncludes() {
        return fetchSourceIncludes;
    }

    public MultiGetQueryOptions setFetchSourceIncludes(List<String> fetchSourceIncludes) {
        this.fetchSourceIncludes = fetchSourceIncludes;
        return this;
    }

    public List<String> getFetchSourceExcludes() {
        return fetchSourceExcludes;
    }

    public MultiGetQueryOptions setFetchSourceExcludes(List<String> fetchSourceExcludes) {
        this.fetchSourceExcludes = fetchSourceExcludes;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (routing != null) json.put(JSON_FIELD_ROUTING, routing);
        if (parent != null) json.put(JSON_FIELD_PARENT, parent);
        if (storedFields != null) json.put(JSON_FIELD_STORED_FIELDS, new JsonArray(storedFields));
        if (fetchSource != null) json.put(JSON_FIELD_FETCH_SOURCE, fetchSource);
        if (fetchSourceIncludes != null) json.put(JSON_FIELD_FETCH_SOURCE_INCLUDES, new JsonArray(fetchSourceIncludes));
        if (fetchSourceExcludes != null) json.put(JSON_FIELD_FETCH_SOURCE_EXCLUDES, new JsonArray(fetchSourceExcludes));

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
