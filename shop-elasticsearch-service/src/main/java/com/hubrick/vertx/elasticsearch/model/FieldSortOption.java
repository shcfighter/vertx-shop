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

/**
 * Sort option
 */
@DataObject
public class FieldSortOption extends BaseSortOption {

    private String field;

    public static final String JSON_FIELD_FIELD = "field";

    public FieldSortOption() {
        super(SortType.FIELD);
    }

    public FieldSortOption(FieldSortOption other) {
        super(other);
        field = other.getField();
    }

    public FieldSortOption(JsonObject json) {
        super(json);

        field = json.getString(JSON_FIELD_FIELD);
    }

    public String getField() {
        return field;
    }

    public FieldSortOption setField(String field) {
        this.field = field;
        return this;
    }

    @Override
    public FieldSortOption setOrder(SortOrder order) {
        super.setOrder(order);
        return this;
    }

    public JsonObject toJson() {
        final JsonObject baseJsonObject = super.toJson();

        final JsonObject jsonObject = new JsonObject()
                .put(JSON_FIELD_FIELD, field);

        jsonObject.mergeIn(baseJsonObject);
        return jsonObject;
    }

}
