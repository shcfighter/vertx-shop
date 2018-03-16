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

import java.util.Optional;

/**
 * Delete by query operation options
 */
@DataObject
public class DeleteByQueryOptions extends AbstractSearchOptions<DeleteByQueryOptions> {

    private Integer maxRetries;
    private Integer slices;
    private Conflicts conflicts;
    private Integer waitForActiveShards;
    private Float requestsPerSecond;

    public static final String JSON_FIELD_MAX_RETRIES = "maxRetries";
    public static final String JSON_FIELD_SLICES = "slices";
    public static final String JSON_FIELD_CONFLICTS = "conflicts";
    public static final String JSON_FIELD_WAIT_FOR_ACTIVE_SHARDS = "waitForActiveShards";
    public static final String JSON_FIELD_REQUESTS_PER_SECOND = "requestsPerSecond";

    public DeleteByQueryOptions() {
    }

    public DeleteByQueryOptions(DeleteByQueryOptions other) {
        super(other);

        maxRetries = other.getMaxRetries();
        slices = getSlices();
        conflicts = other.getConflicts();
        waitForActiveShards = other.getWaitForActiveShards();
        requestsPerSecond = other.getRequestsPerSecond();
    }

    public DeleteByQueryOptions(JsonObject json) {
        super(json);

        maxRetries = json.getInteger(JSON_FIELD_MAX_RETRIES);
        slices = json.getInteger(JSON_FIELD_SLICES);
        conflicts = Optional.ofNullable(json.getString(JSON_FIELD_CONFLICTS, null)).map(Conflicts::valueOf).orElse(null);
        waitForActiveShards = json.getInteger(JSON_FIELD_WAIT_FOR_ACTIVE_SHARDS);
        requestsPerSecond = json.getFloat(JSON_FIELD_REQUESTS_PER_SECOND);
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public DeleteByQueryOptions setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public Integer getSlices() {
        return slices;
    }

    public DeleteByQueryOptions setSlices(Integer slices) {
        this.slices = slices;
        return this;
    }

    public Conflicts getConflicts() {
        return conflicts;
    }

    public DeleteByQueryOptions setConflicts(Conflicts conflicts) {
        this.conflicts = conflicts;
        return this;
    }

    public Integer getWaitForActiveShards() {
        return waitForActiveShards;
    }

    public DeleteByQueryOptions setWaitForActiveShards(Integer waitForActiveShards) {
        this.waitForActiveShards = waitForActiveShards;
        return this;
    }

    public Float getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public DeleteByQueryOptions setRequestsPerSecond(Float requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = super.toJson();

        if (maxRetries != null) json.put(JSON_FIELD_MAX_RETRIES, maxRetries);
        if (slices != null) json.put(JSON_FIELD_SLICES, maxRetries);
        if (conflicts != null) json.put(JSON_FIELD_CONFLICTS, conflicts.name());
        if (waitForActiveShards != null) json.put(JSON_FIELD_WAIT_FOR_ACTIVE_SHARDS, waitForActiveShards);
        if (requestsPerSecond != null) json.put(JSON_FIELD_REQUESTS_PER_SECOND, requestsPerSecond);

        return json;
    }

}
