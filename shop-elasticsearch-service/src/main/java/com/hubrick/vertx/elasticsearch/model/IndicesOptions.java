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
 * Multisearch operation options
 *
 * @author Emir Dizdarevic
 * @since 2.2.0
 */
@DataObject
public class IndicesOptions {

    private static final String JSON_FIELD_IGNORE_UNAVAILABLE = "ignoreUnavailable";
    private static final String JSON_FIELD_ALLOW_NO_INDICES = "allowNoIndices";
    private static final String JSON_FIELD_EXPAND_TO_OPEN_INDICES = "expandToOpenIndices";
    private static final String JSON_FIELD_EXPAND_TO_CLOSE_INDICES = "expandToClosedIndices";
    private static final String JSON_FIELD_ALLOW_ALIASES_TO_MULTIPLE_INDICES = "allowAliasesToMultipleIndices";
    private static final String JSON_FIELD_FORBID_CLOSED_INDICES = "forbidClosedIndices";
    private static final String JSON_FIELD_IGNORE_ALIASES = "ignoreAliases";

    private Boolean ignoreUnavailable;
    private Boolean allowNoIndices;
    private Boolean expandToOpenIndices;
    private Boolean expandToClosedIndices;
    private Boolean allowAliasesToMultipleIndices;
    private Boolean forbidClosedIndices;
    private Boolean ignoreAliases;

    public IndicesOptions() {
    }

    public IndicesOptions(IndicesOptions other) {
        this.ignoreUnavailable = other.getIgnoreUnavailable();
        this.allowNoIndices = other.getAllowNoIndices();
        this.expandToOpenIndices = other.getExpandToOpenIndices();
        this.expandToClosedIndices = other.getExpandToClosedIndices();
        this.allowAliasesToMultipleIndices = other.getAllowAliasesToMultipleIndices();
        this.forbidClosedIndices = other.getForbidClosedIndices();
        this.ignoreAliases = other.getIgnoreAliases();
    }

    public IndicesOptions(JsonObject json) {
        this.ignoreUnavailable = json.getBoolean(JSON_FIELD_IGNORE_UNAVAILABLE);
        this.allowNoIndices = json.getBoolean(JSON_FIELD_ALLOW_NO_INDICES);
        this.expandToOpenIndices = json.getBoolean(JSON_FIELD_EXPAND_TO_OPEN_INDICES);
        this.expandToClosedIndices = json.getBoolean(JSON_FIELD_EXPAND_TO_CLOSE_INDICES);
        this.allowAliasesToMultipleIndices = json.getBoolean(JSON_FIELD_ALLOW_ALIASES_TO_MULTIPLE_INDICES);
        this.forbidClosedIndices = json.getBoolean(JSON_FIELD_FORBID_CLOSED_INDICES);
        this.ignoreAliases = json.getBoolean(JSON_FIELD_IGNORE_ALIASES);
    }

    public Boolean getIgnoreUnavailable() {
        return ignoreUnavailable;
    }

    public IndicesOptions setIgnoreUnavailable(Boolean ignoreUnavailable) {
        this.ignoreUnavailable = ignoreUnavailable;
        return this;
    }

    public Boolean getAllowNoIndices() {
        return allowNoIndices;
    }

    public IndicesOptions setAllowNoIndices(Boolean allowNoIndices) {
        this.allowNoIndices = allowNoIndices;
        return this;
    }

    public Boolean getExpandToOpenIndices() {
        return expandToOpenIndices;
    }

    public IndicesOptions setExpandToOpenIndices(Boolean expandToOpenIndices) {
        this.expandToOpenIndices = expandToOpenIndices;
        return this;
    }

    public Boolean getExpandToClosedIndices() {
        return expandToClosedIndices;
    }

    public IndicesOptions setExpandToClosedIndices(Boolean expandToClosedIndices) {
        this.expandToClosedIndices = expandToClosedIndices;
        return this;
    }

    public Boolean getAllowAliasesToMultipleIndices() {
        return allowAliasesToMultipleIndices;
    }

    public IndicesOptions setAllowAliasesToMultipleIndices(Boolean allowAliasesToMultipleIndices) {
        this.allowAliasesToMultipleIndices = allowAliasesToMultipleIndices;
        return this;
    }

    public Boolean getForbidClosedIndices() {
        return forbidClosedIndices;
    }

    public IndicesOptions setForbidClosedIndices(Boolean forbidClosedIndices) {
        this.forbidClosedIndices = forbidClosedIndices;
        return this;
    }

    public Boolean getIgnoreAliases() {
        return ignoreAliases;
    }

    public IndicesOptions setIgnoreAliases(Boolean ignoreAliases) {
        this.ignoreAliases = ignoreAliases;
        return this;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();

        if (ignoreUnavailable != null) json.put(JSON_FIELD_IGNORE_UNAVAILABLE, ignoreUnavailable);
        if (allowNoIndices != null) json.put(JSON_FIELD_ALLOW_NO_INDICES, allowNoIndices);
        if (expandToOpenIndices != null) json.put(JSON_FIELD_EXPAND_TO_OPEN_INDICES, expandToOpenIndices);
        if (expandToClosedIndices != null) json.put(JSON_FIELD_EXPAND_TO_CLOSE_INDICES, expandToClosedIndices);
        if (allowAliasesToMultipleIndices != null) json.put(JSON_FIELD_ALLOW_ALIASES_TO_MULTIPLE_INDICES, allowAliasesToMultipleIndices);
        if (forbidClosedIndices != null) json.put(JSON_FIELD_FORBID_CLOSED_INDICES, forbidClosedIndices);
        if (ignoreAliases != null) json.put(JSON_FIELD_IGNORE_ALIASES, ignoreAliases);

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
