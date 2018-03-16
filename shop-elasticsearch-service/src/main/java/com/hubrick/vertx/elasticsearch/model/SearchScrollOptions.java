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
 * Search scroll options
 */
@DataObject
public class SearchScrollOptions {

    private String scroll;

    public static final String FIELD_SCROLL = "scroll";

    public SearchScrollOptions() {
    }

    public SearchScrollOptions(SearchScrollOptions other) {
        scroll = other.getScroll();
    }

    public SearchScrollOptions(JsonObject json) {
        scroll = json.getString(FIELD_SCROLL);
    }

    public String getScroll() {
        return scroll;
    }

    public SearchScrollOptions setScroll(String keepAlive) {
        this.scroll = keepAlive;
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();

        if (getScroll() != null) json.put(FIELD_SCROLL, getScroll());

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
