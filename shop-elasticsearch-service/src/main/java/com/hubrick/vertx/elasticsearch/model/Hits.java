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
public class Hits {

    private Long total;
    private Float maxScore;
    private List<Hit> hits = new LinkedList<>();

    public static final String JSON_FIELD_TOTAL = "total";
    public static final String JSON_FIELD_MAX_SCORE = "maxScore";
    public static final String JSON_FIELD_HITS = "hits";

    public Hits() {}

    public Hits(Hits other) {
        this.total = other.getTotal();
        this.maxScore = other.getMaxScore();
        this.hits = other.getHits();
    }

    public Hits(JsonObject jsonObject) {
        this.total = jsonObject.getLong(JSON_FIELD_TOTAL);
        this.maxScore = jsonObject.getFloat(JSON_FIELD_MAX_SCORE);

        final JsonArray jsonHits = jsonObject.getJsonArray(JSON_FIELD_HITS);
        if(jsonHits != null) {
            for (int i=0;i<jsonHits.size();i++) {
                hits.add(new Hit(jsonHits.getJsonObject(i)));
            }
        }
    }

    public Long getTotal() {
        return total;
    }

    public Hits setTotal(Long total) {
        this.total = total;
        return this;
    }

    public Float getMaxScore() {
        return maxScore;
    }

    public Hits setMaxScore(Float maxScore) {
        this.maxScore = maxScore;
        return this;
    }

    public List<Hit> getHits() {
        return hits;
    }

    public Hits setHits(List<Hit> hits) {
        this.hits = hits;
        return this;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (total != null) json.put(JSON_FIELD_TOTAL, total);
        if (maxScore != null) json.put(JSON_FIELD_MAX_SCORE, maxScore);
        if (!hits.isEmpty()) {
            final JsonArray hitsJson = new JsonArray();
            hits.forEach(hit -> hitsJson.add(hit.toJson()));
            json.put(JSON_FIELD_HITS, hitsJson);
        }

        return json;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
