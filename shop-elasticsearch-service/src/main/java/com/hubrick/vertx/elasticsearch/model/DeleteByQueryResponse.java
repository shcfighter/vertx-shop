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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Optional;

@DataObject
public class DeleteByQueryResponse extends AbstractRawResponse<DeleteByQueryResponse> {

    private Long tookMillis;
    private Boolean timedOut;
    private Long deleted;
    private Integer batches;
    private Long versionConflicts;
    private Retries retries;
    private Long throttledMillis;
    private Float requestsPerSecond;
    private Long throttledUntilMillis;
    private Long total;
    private JsonArray failures = new JsonArray();

    public static final String JSON_FIELD_TOOK_MILLIS = "tookMillis";
    public static final String JSON_FIELD_TIMED_OUT = "timedOut";
    public static final String JSON_FIELD_DELETED = "deleted";
    public static final String JSON_FIELD_BATCHES = "batches";
    public static final String JSON_FIELD_VERSION_CONFLICTS = "versionConflicts";
    public static final String JSON_FIELD_RETRIES = "retries";
    public static final String JSON_FIELD_THROTTLED_MILLIS = "throttledMillis";
    public static final String JSON_FIELD_REQUESTS_PER_SECOND = "requestsPerSecond";
    public static final String JSON_FIELD_THROTTLED_UNTIL_MILLIS = "throttledUntilMillis";
    public static final String JSON_FIELD_TOTAL = "total";
    public static final String JSON_FIELD_FAILURES = "failures";

    public DeleteByQueryResponse() {
    }

    public DeleteByQueryResponse(DeleteByQueryResponse other) {
        super(other);

        this.tookMillis = other.getTookMillis();
        this.timedOut = other.getTimedOut();
        this.deleted = other.getDeleted();
        this.batches = other.getBatches();
        this.versionConflicts = other.getVersionConflicts();
        this.retries = other.getRetries();
        this.throttledMillis = other.getThrottledMillis();
        this.requestsPerSecond = other.getRequestsPerSecond();
        this.throttledUntilMillis = other.getThrottledUntilMillis();
        this.total = other.getTotal();
        this.failures = other.getFailures();
    }

    public DeleteByQueryResponse(JsonObject json) {
        super(json);

        this.tookMillis = json.getLong(JSON_FIELD_TOOK_MILLIS);
        this.timedOut = json.getBoolean(JSON_FIELD_TIMED_OUT);
        this.deleted = json.getLong(JSON_FIELD_DELETED);
        this.batches = json.getInteger(JSON_FIELD_BATCHES);
        this.versionConflicts = json.getLong(JSON_FIELD_VERSION_CONFLICTS);
        this.retries = Optional.ofNullable(json.getJsonObject(JSON_FIELD_RETRIES)).map(Retries::new).orElse(null);
        this.throttledMillis = json.getLong(JSON_FIELD_THROTTLED_MILLIS);
        this.requestsPerSecond = json.getFloat(JSON_FIELD_REQUESTS_PER_SECOND);
        this.throttledUntilMillis = json.getLong(JSON_FIELD_THROTTLED_UNTIL_MILLIS);
        this.total = json.getLong(JSON_FIELD_TOTAL);
        this.failures = json.getJsonArray(JSON_FIELD_FAILURES);
    }

    public Long getTookMillis() {
        return tookMillis;
    }

    public void setTookMillis(Long tookMillis) {
        this.tookMillis = tookMillis;
    }

    public Boolean getTimedOut() {
        return timedOut;
    }

    public void setTimedOut(Boolean timedOut) {
        this.timedOut = timedOut;
    }

    public Long getDeleted() {
        return deleted;
    }

    public void setDeleted(Long deleted) {
        this.deleted = deleted;
    }

    public Integer getBatches() {
        return batches;
    }

    public void setBatches(Integer batches) {
        this.batches = batches;
    }

    public Long getVersionConflicts() {
        return versionConflicts;
    }

    public void setVersionConflicts(Long versionConflicts) {
        this.versionConflicts = versionConflicts;
    }

    public Retries getRetries() {
        return retries;
    }

    public void setRetries(Retries retries) {
        this.retries = retries;
    }

    public Long getThrottledMillis() {
        return throttledMillis;
    }

    public void setThrottledMillis(Long throttledMillis) {
        this.throttledMillis = throttledMillis;
    }

    public Float getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(Float requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public Long getThrottledUntilMillis() {
        return throttledUntilMillis;
    }

    public void setThrottledUntilMillis(Long throttledUntilMillis) {
        this.throttledUntilMillis = throttledUntilMillis;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public JsonArray getFailures() {
        return failures;
    }

    @GenIgnore
    public DeleteByQueryResponse addFailure(JsonObject failure) {
        this.failures.add(failure);
        return this;
    }

    public DeleteByQueryResponse setFailures(JsonArray failures) {
        this.failures = failures;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (tookMillis != null) json.put(JSON_FIELD_TOOK_MILLIS, tookMillis);
        if (timedOut != null) json.put(JSON_FIELD_TIMED_OUT, timedOut);
        if (deleted != null) json.put(JSON_FIELD_DELETED, deleted);
        if (batches != null) json.put(JSON_FIELD_BATCHES, batches);
        if (versionConflicts != null) json.put(JSON_FIELD_VERSION_CONFLICTS, versionConflicts);
        if (retries != null) json.put(JSON_FIELD_RETRIES, retries.toJson());
        if (throttledMillis != null) json.put(JSON_FIELD_THROTTLED_MILLIS, throttledMillis);
        if (requestsPerSecond != null) json.put(JSON_FIELD_REQUESTS_PER_SECOND, requestsPerSecond);
        if (throttledUntilMillis != null) json.put(JSON_FIELD_THROTTLED_UNTIL_MILLIS, throttledUntilMillis);
        if (total != null) json.put(JSON_FIELD_TOTAL, total);
        if (failures != null) json.put(JSON_FIELD_FAILURES, failures);

        return json.mergeIn(super.toJson());
    }
}
