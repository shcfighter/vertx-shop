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
public class BulkDeleteOptions {

    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_DELETE_OPTIONS = "deleteOptions";

    private String index;
    private String type;
    private String id;
    private DeleteOptions deleteOptions;

    public BulkDeleteOptions() {
    }

    public BulkDeleteOptions(JsonObject json) {
        this.index = json.getString(JSON_FIELD_INDEX);
        this.type = json.getString(JSON_FIELD_TYPE);
        this.id = json.getString(JSON_FIELD_ID);
        this.deleteOptions = Optional.ofNullable(json.getJsonObject(JSON_FIELD_DELETE_OPTIONS)).map(DeleteOptions::new).orElse(null);
    }

    public String getIndex() {
        return index;
    }

    public BulkDeleteOptions setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public BulkDeleteOptions setType(String type) {
        this.type = type;
        return this;
    }

    public String getId() {
        return id;
    }

    public BulkDeleteOptions setSource(String id) {
        this.id = id;
        return this;
    }

    public DeleteOptions getDeleteOptions() {
        return deleteOptions;
    }

    public BulkDeleteOptions setDeleteOptions(DeleteOptions deleteOptions) {
        this.deleteOptions = deleteOptions;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (id != null) json.put(JSON_FIELD_ID, id);
        if (deleteOptions != null) json.put(JSON_FIELD_DELETE_OPTIONS, deleteOptions.toJson());

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
