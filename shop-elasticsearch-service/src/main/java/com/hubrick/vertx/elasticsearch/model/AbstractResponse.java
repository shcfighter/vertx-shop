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

import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Search operation options
 */
public abstract class AbstractResponse<T extends AbstractResponse<T>> {

    private Shards shards;

    public static final String JSON_FIELD_SHARDS = "shards";

    protected AbstractResponse() {
    }

    public AbstractResponse(AbstractResponse other) {
        this.shards = other.getShards();
    }

    public AbstractResponse(JsonObject json) {

        final JsonObject jsonShards = json.getJsonObject(JSON_FIELD_SHARDS);
        if (jsonShards != null) {
            this.shards = new Shards(jsonShards);
        }
    }

    public Shards getShards() {
        return shards;
    }

    public T setShards(Shards shards) {
        this.shards = shards;
        return returnThis();
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (shards != null) json.put(JSON_FIELD_SHARDS, shards.toJson());

        return json;
    }

    protected T returnThis() {
        return (T) this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
