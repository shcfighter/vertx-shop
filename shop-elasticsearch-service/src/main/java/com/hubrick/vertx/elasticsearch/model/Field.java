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
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
@DataObject
public class Field {

    private String name;
    private List<Object> values = new LinkedList<>();

    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_VALUES = "values";

    public Field() {
    }

    public Field(Field other) {
        this.name = other.getName();
        this.values = other.getValues();
    }

    public Field(JsonObject jsonObject) {
        this.name = jsonObject.getString(JSON_FIELD_NAME);

        final JsonArray jsonFieldValues = jsonObject.getJsonArray(JSON_FIELD_VALUES);
        if (jsonFieldValues != null) {
            jsonFieldValues.stream().forEach(e -> values.add(e));
        }
    }

    public String getName() {
        return name;
    }

    public List<Object> getValues() {
        return values;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (name != null) json.put(JSON_FIELD_NAME, name);
        if (!values.isEmpty()) {
            final JsonArray jsonArray = new JsonArray();
            values.stream().forEach(e -> jsonArray.add(e));
            json.put(JSON_FIELD_VALUES, jsonArray);
        }

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
