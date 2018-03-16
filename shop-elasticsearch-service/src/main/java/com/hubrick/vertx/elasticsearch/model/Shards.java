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
 * @since 1.0.0
 */
@DataObject
public class Shards {

    private Integer total;
    private Integer successful;
    private Integer failed;

    public static final String JSON_FIELD_TOTAL = "total";
    public static final String JSON_FIELD_SUCCESSFUL = "successful";
    public static final String JSON_FIELD_FAILED = "failed";

    public Shards() {}

    public Shards(Shards other) {
        this.total = other.getTotal();
        this.successful = other.getSuccessful();
        this.failed = other.getFailed();
    }

    public Shards(JsonObject jsonObject) {
        this.total = jsonObject.getInteger(JSON_FIELD_TOTAL);
        this.successful = jsonObject.getInteger(JSON_FIELD_SUCCESSFUL);
        this.failed = jsonObject.getInteger(JSON_FIELD_FAILED);
    }

    public Integer getTotal() {
        return total;
    }

    public Shards setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public Integer getSuccessful() {
        return successful;
    }

    public Shards setSuccessful(Integer successful) {
        this.successful = successful;
        return this;
    }

    public Integer getFailed() {
        return failed;
    }

    public Shards setFailed(Integer failed) {
        this.failed = failed;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (total != null) json.put(JSON_FIELD_TOTAL, total);
        if (successful != null) json.put(JSON_FIELD_SUCCESSFUL, successful);
        if (failed != null) json.put(JSON_FIELD_FAILED, failed);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
