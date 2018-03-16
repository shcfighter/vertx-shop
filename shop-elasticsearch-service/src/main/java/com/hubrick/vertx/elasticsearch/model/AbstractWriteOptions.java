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

import java.util.Optional;

/**
 * Abstract options
 */
public abstract class AbstractWriteOptions<T extends AbstractWriteOptions<T>> extends AbstractOptions<T> {

    private RefreshPolicy refreshPolicy;
    private Integer waitForActiveShard;
    private String timeout;

    public static final String FIELD_REFRESH_POLICY = "refreshPolicy";
    public static final String FIELD_WAIT_FOR_ACTIVE_SHARD = "waitForActiveShard";
    public static final String FIELD_TIMEOUT = "timeout";

    protected AbstractWriteOptions() {
    }

    protected AbstractWriteOptions(T other) {
        super(other);
        refreshPolicy = other.getRefreshPolicy();
        waitForActiveShard = other.getWaitForActiveShard();
        timeout = other.getTimeout();
    }

    protected AbstractWriteOptions(JsonObject json) {
        super(json);

        refreshPolicy = Optional.ofNullable(json.getString(FIELD_REFRESH_POLICY)).map(RefreshPolicy::valueOf).orElse(null);
        timeout = json.getString(FIELD_TIMEOUT);
        waitForActiveShard = json.getInteger(FIELD_WAIT_FOR_ACTIVE_SHARD);
    }

    public RefreshPolicy getRefreshPolicy() {
        return refreshPolicy;
    }

    public T setRefresh(RefreshPolicy refreshPolicy) {
        this.refreshPolicy = refreshPolicy;
        return returnThis();
    }

    public Integer getWaitForActiveShard() {
        return waitForActiveShard;
    }

    public T setWaitForActiveShard(Integer waitForActiveShard) {
        this.waitForActiveShard = waitForActiveShard;
        return returnThis();
    }

    public String getTimeout() {
        return timeout;
    }

    public T setTimeout(String timeout) {
        this.timeout = timeout;
        return returnThis();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = super.toJson();

        if (getRefreshPolicy() != null) json.put(FIELD_REFRESH_POLICY, getRefreshPolicy().name());
        if (getWaitForActiveShard() != null) {
            json.put(FIELD_WAIT_FOR_ACTIVE_SHARD, getWaitForActiveShard());
        }
        if (getTimeout() != null) json.put(FIELD_TIMEOUT, getTimeout());

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
