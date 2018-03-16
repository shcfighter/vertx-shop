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
public class BulkIndexOptions {

    public static final String JSON_FIELD_INDEX = "index";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_SOURCE = "source";
    public static final String JSON_FIELD_INDEX_OPTIONS = "indexOptions";

    private String index;
    private String type;
    private JsonObject source;
    private IndexOptions indexOptions;

    public BulkIndexOptions() {
    }

    public BulkIndexOptions(JsonObject json) {
        this.index = json.getString(JSON_FIELD_INDEX);
        this.type = json.getString(JSON_FIELD_TYPE);
        this.source = json.getJsonObject(JSON_FIELD_SOURCE);
        this.indexOptions = Optional.ofNullable(json.getJsonObject(JSON_FIELD_INDEX_OPTIONS)).map(IndexOptions::new).orElse(null);
    }

    public String getIndex() {
        return index;
    }

    public BulkIndexOptions setIndex(String index) {
        this.index = index;
        return this;
    }

    public String getType() {
        return type;
    }

    public BulkIndexOptions setType(String type) {
        this.type = type;
        return this;
    }

    public JsonObject getSource() {
        return source;
    }

    public BulkIndexOptions setSource(JsonObject source) {
        this.source = source;
        return this;
    }

    public IndexOptions getIndexOptions() {
        return indexOptions;
    }

    public BulkIndexOptions setIndexOptions(IndexOptions indexOptions) {
        this.indexOptions = indexOptions;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (index != null) json.put(JSON_FIELD_INDEX, index);
        if (type != null) json.put(JSON_FIELD_TYPE, type);
        if (source != null) json.put(JSON_FIELD_SOURCE, source);
        if (indexOptions != null) json.put(JSON_FIELD_INDEX_OPTIONS, indexOptions.toJson());

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
