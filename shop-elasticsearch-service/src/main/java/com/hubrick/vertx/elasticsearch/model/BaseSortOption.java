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
 * Sort option
 *
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public abstract class BaseSortOption {

    private SortType sortType;
    private SortOrder order;

    public static final String JSON_FIELD_SORT_TYPE = "sortType";
    public static final String JSON_FIELD_ORDER = "order";

    protected BaseSortOption(SortType sortSortType) {
        this.sortType = sortSortType;
    }

    public BaseSortOption(BaseSortOption other) {
        order = other.getOrder();
        sortType = other.getSortType();
    }

    public BaseSortOption(JsonObject json) {
        this.order = Optional.ofNullable(json.getString(JSON_FIELD_ORDER)).map(SortOrder::valueOf).orElse(null);
        this.sortType = Optional.ofNullable(json.getString(JSON_FIELD_SORT_TYPE)).map(SortType::valueOf).orElse(null);
    }

    public SortOrder getOrder() {
        return order;
    }

    public BaseSortOption setOrder(SortOrder order) {
        this.order = order;
        return this;
    }

    public SortType getSortType() {
        return sortType;
    }

    public JsonObject toJson() {
        final JsonObject jsonObject = new JsonObject();

        if(order != null) jsonObject.put(JSON_FIELD_ORDER, order.name());
        if(sortType != null) jsonObject.put(JSON_FIELD_SORT_TYPE, sortType.name());

        return jsonObject;
    }

    public static BaseSortOption parseJson(JsonObject jsonObject) {
        try {
            final SortType sortType = SortType.valueOf(jsonObject.getString(JSON_FIELD_SORT_TYPE));
            switch (sortType) {
                case FIELD:
                    return new FieldSortOption(jsonObject);
                case SCRIPT:
                    return new ScriptSortOption(jsonObject);
                default:
                    throw new IllegalArgumentException("SortType " + jsonObject.getString(JSON_FIELD_SORT_TYPE) + " is not supported");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("SortType " + jsonObject.getString(JSON_FIELD_SORT_TYPE) + " is not supported");
        }
    }

    public enum SortType {
        FIELD,
        SCRIPT
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
