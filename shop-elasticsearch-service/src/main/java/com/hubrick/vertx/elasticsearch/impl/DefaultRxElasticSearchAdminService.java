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
package com.hubrick.vertx.elasticsearch.impl;

import com.hubrick.vertx.elasticsearch.ElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.model.CreateIndexOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteIndexOptions;
import com.hubrick.vertx.elasticsearch.model.MappingOptions;
import com.hubrick.vertx.elasticsearch.RxElasticSearchAdminService;
import com.hubrick.vertx.elasticsearch.model.TemplateOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import rx.Observable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Emir Dizdarevic
 * @since 1.0.0
 */
public class DefaultRxElasticSearchAdminService implements RxElasticSearchAdminService {

    private final ElasticSearchAdminService elasticSearchAdminService;

    public DefaultRxElasticSearchAdminService(ElasticSearchAdminService elasticSearchAdminService) {
        checkNotNull(elasticSearchAdminService, "elasticSearchAdminService must not be null");

        this.elasticSearchAdminService = elasticSearchAdminService;
    }

    @Override
    public Observable<Void> putMapping(List<String> indices, String type, JsonObject source, MappingOptions options) {
        final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
        elasticSearchAdminService.putMapping(indices, type, source, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<Void> createIndex(String index, JsonObject source, CreateIndexOptions options) {
        final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
        elasticSearchAdminService.createIndex(index, source, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<Void> deleteIndex(List<String> indices, DeleteIndexOptions options) {
        final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
        elasticSearchAdminService.deleteIndex(indices, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<Void> putTemplate(String name, JsonObject source, TemplateOptions options) {
        final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
        elasticSearchAdminService.putTemplate(name, source, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<Void> deleteTemplate(String name, TemplateOptions options) {
        final ObservableFuture<Void> observableFuture = RxHelper.observableFuture();
        elasticSearchAdminService.deleteTemplate(name, options, observableFuture.toHandler());
        return observableFuture;
    }
}
