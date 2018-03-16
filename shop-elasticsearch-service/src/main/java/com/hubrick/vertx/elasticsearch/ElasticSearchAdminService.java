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
package com.hubrick.vertx.elasticsearch;

import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteIndexOptions;
import com.hubrick.vertx.elasticsearch.model.MappingOptions;
import com.hubrick.vertx.elasticsearch.model.TemplateOptions;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.ProxyIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Admin service
 */
@VertxGen
@ProxyGen
public interface ElasticSearchAdminService {

    static ElasticSearchAdminService createEventBusProxy(Vertx vertx, String address) {
        return new ServiceProxyBuilder(vertx).setAddress(address).build(ElasticSearchAdminService.class);
    }

    @GenIgnore
    @ProxyIgnore
    default void putMapping(String index, String type, JsonObject source, Handler<AsyncResult<Void>> resultHandler) {
        putMapping(Collections.singletonList(index), type, source, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void putMapping(String index, String type, JsonObject source, MappingOptions options, Handler<AsyncResult<Void>> resultHandler) {
        putMapping(Collections.singletonList(index), type, source, options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void putMapping(List<String> indices, String type, JsonObject source, Handler<AsyncResult<Void>> resultHandler) {
        putMapping(indices, type, source, null, resultHandler);
    }

    void putMapping(List<String> indices, String type, JsonObject source, MappingOptions options, Handler<AsyncResult<Void>> resultHandler);

    void createIndex(String index, JsonObject source, CreateIndexOptions options, Handler<AsyncResult<Void>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void deleteIndex(String index, Handler<AsyncResult<Void>> resultHandler) {
        deleteIndex(Collections.singletonList(index), new DeleteIndexOptions(), resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void deleteIndex(String index, DeleteIndexOptions options, Handler<AsyncResult<Void>> resultHandler) {
        deleteIndex(Collections.singletonList(index), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void deleteIndex(List<String> indices, Handler<AsyncResult<Void>> resultHandler) {
        deleteIndex(indices, new DeleteIndexOptions(), resultHandler);
    }

    void deleteIndex(List<String> indices, DeleteIndexOptions options, Handler<AsyncResult<Void>> resultHandler);


    void putTemplate(String name, JsonObject source, TemplateOptions options, Handler<AsyncResult<Void>> resultHandler);
    void deleteTemplate(String name, TemplateOptions options, Handler<AsyncResult<Void>> resultHandler);
}
