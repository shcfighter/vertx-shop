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
 * MultiGet operation options
 *
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class MultiGetOptions {

    private static final String JSON_FIELD_REFRESH = "refresh";
    private static final String JSON_FIELD_REALTIME = "realtime";
    private static final String JSON_FIELD_PREFERENCE = "preference";

    private Boolean refresh;
    private Boolean realtime;
    private String preference;

    public MultiGetOptions() {
    }

    public MultiGetOptions(MultiGetOptions other) {
        this.refresh = other.getRefresh();
        this.realtime = other.getRealtime();
        this.preference = other.getPreference();
    }

    public MultiGetOptions(JsonObject json) {
        this.refresh = json.getBoolean(JSON_FIELD_REFRESH);
        this.realtime = json.getBoolean(JSON_FIELD_REALTIME);
        this.preference = json.getString(JSON_FIELD_PREFERENCE);
    }

    public Boolean getRefresh() {
        return refresh;
    }

    public MultiGetOptions setRefresh(Boolean refresh) {
        this.refresh = refresh;
        return this;
    }

    public Boolean getRealtime() {
        return realtime;
    }

    public MultiGetOptions setRealtime(Boolean realtime) {
        this.realtime = realtime;
        return this;
    }

    public String getPreference() {
        return preference;
    }

    public MultiGetOptions setPreference(String preference) {
        this.preference = preference;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (refresh != null) json.put(JSON_FIELD_REFRESH, refresh);
        if (realtime != null) json.put(JSON_FIELD_REALTIME, realtime);
        if (preference != null) json.put(JSON_FIELD_PREFERENCE, preference);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
