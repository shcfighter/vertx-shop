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

import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.BulkDeleteOptions;
import com.hubrick.vertx.elasticsearch.model.BulkIndexOptions;
import com.hubrick.vertx.elasticsearch.model.BulkOptions;
import com.hubrick.vertx.elasticsearch.model.BulkResponse;
import com.hubrick.vertx.elasticsearch.model.BulkUpdateOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteByQueryResponse;
import com.hubrick.vertx.elasticsearch.model.DeleteOptions;
import com.hubrick.vertx.elasticsearch.model.DeleteResponse;
import com.hubrick.vertx.elasticsearch.model.GetOptions;
import com.hubrick.vertx.elasticsearch.model.GetResponse;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.IndexResponse;
import com.hubrick.vertx.elasticsearch.model.MultiGetOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiGetResponse;
import com.hubrick.vertx.elasticsearch.model.MultiSearchOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchQueryOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateResponse;
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
public class DefaultRxElasticSearchService implements RxElasticSearchService {

    private final ElasticSearchService elasticSearchService;

    public DefaultRxElasticSearchService(ElasticSearchService elasticSearchService) {
        checkNotNull(elasticSearchService, "elasticSearchService must not be null");

        this.elasticSearchService = elasticSearchService;
    }

    @Override
    public Observable<IndexResponse> index(String index, String type, JsonObject source, IndexOptions options) {
        final ObservableFuture<IndexResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.index(index, type, source, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<UpdateResponse> update(String index, String type, String id, UpdateOptions options) {
        final ObservableFuture<UpdateResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.update(index, type, id, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<GetResponse> get(String index, String type, String id, GetOptions options) {
        final ObservableFuture<GetResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.get(index, type, id, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<SearchResponse> search(List<String> indices, SearchOptions options) {
        final ObservableFuture<SearchResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.search(indices, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<SearchResponse> searchScroll(String scrollId, SearchScrollOptions options) {
        final ObservableFuture<SearchResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.searchScroll(scrollId, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<DeleteResponse> delete(String index, String type, String id, DeleteOptions options) {
        final ObservableFuture<DeleteResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.delete(index, type, id, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<BulkResponse> bulk(List<BulkIndexOptions> bulkIndexOptions,
                                         List<BulkUpdateOptions> bulkUpdateOptions,
                                         List<BulkDeleteOptions> bulkDeleteOptions,
                                         BulkOptions bulkOptions) {
        final ObservableFuture<BulkResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.bulk(bulkIndexOptions, bulkUpdateOptions, bulkDeleteOptions, bulkOptions, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<MultiSearchResponse> multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions, MultiSearchOptions options) {
        final ObservableFuture<MultiSearchResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.multiSearch(multiSearchQueryOptions, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<MultiGetResponse> multiGet(List<MultiGetQueryOptions> multiGetQueryOptions, MultiGetOptions options) {
        final ObservableFuture<MultiGetResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.multiGet(multiGetQueryOptions, options, observableFuture.toHandler());
        return observableFuture;
    }

    @Override
    public Observable<DeleteByQueryResponse> deleteByQuery(List<String> indices, DeleteByQueryOptions options) {
        final ObservableFuture<DeleteByQueryResponse> observableFuture = RxHelper.observableFuture();
        elasticSearchService.deleteByQuery(indices, options, observableFuture.toHandler());
        return observableFuture;
    }
}
