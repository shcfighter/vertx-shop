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

/**
 * @author Emir Dizdarevic
 * @since 2.0.0
 */
@DataObject
public class Retries {

    private Long bulk;
    private Long search;

    public static final String JSON_FIELD_BULK = "bulk";
    public static final String JSON_FIELD_SEARCH = "search";

    public Retries() {
    }

    public Retries(Long bulk, Long search) {
        this.bulk = bulk;
        this.search = search;
    }

    public Retries(Retries other) {
        this.bulk = other.getBulk();
        this.search = other.getSearch();
    }

    public Retries(JsonObject jsonObject) {
        this.bulk = jsonObject.getLong(JSON_FIELD_BULK);
        this.search = jsonObject.getLong(JSON_FIELD_SEARCH);
    }

    public Long getBulk() {
        return bulk;
    }

    public Long getSearch() {
        return search;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (bulk != null) json.put(JSON_FIELD_BULK, bulk);
        if (search != null) json.put(JSON_FIELD_SEARCH, search);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
