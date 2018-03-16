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

@DataObject
public class GetResponse extends AbstractRawResponse<GetResponse> {

    private GetResult result;

    public static final String JSON_FIELD_RESULT = "result";

    public GetResponse() {
    }

    public GetResponse(GetResponse other) {
        super(other);

        this.result = other.getResult();
    }

    public GetResponse(JsonObject json) {
        super(json);

        final JsonObject jsonResult = json.getJsonObject(JSON_FIELD_RESULT);
        if (jsonResult != null) {
            this.result = new GetResult(jsonResult);
        }
    }

    public GetResult getResult() {
        return result;
    }

    public void setResult(GetResult result) {
        this.result = result;
    }

    public JsonObject toJson() {

        final JsonObject json = new JsonObject();

        if (result != null) json.put(JSON_FIELD_RESULT, result.toJson());

        return json.mergeIn(super.toJson());
    }
}
