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
import com.hubrick.vertx.elasticsearch.model.MultiSearchOptions;
import com.hubrick.vertx.elasticsearch.model.MultiSearchQueryOptions;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchScrollOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateOptions;
import com.hubrick.vertx.elasticsearch.model.UpdateResponse;
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
 * ElasticSearch service
 */
@VertxGen
@ProxyGen
public interface ElasticSearchService {

    static ElasticSearchService createEventBusProxy(Vertx vertx, String address) {
        return new ServiceProxyBuilder(vertx).setAddress(address).build(ElasticSearchService.class);
    }

    @ProxyIgnore
    void start();

    @ProxyIgnore
    void stop();

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param source        the source to be indexed
     * @param resultHandler result handler callback
     */
    @GenIgnore
    @ProxyIgnore
    default void index(String index, String type, JsonObject source, Handler<AsyncResult<IndexResponse>> resultHandler) {
        index(index, type, source, new IndexOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/index_.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param source        the source to be indexed
     * @param options       optional index options (id, timeout, ttl, etc.)
     * @param resultHandler result handler callback
     */
    void index(String index, String type, JsonObject source, IndexOptions options, Handler<AsyncResult<IndexResponse>> resultHandler);

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/java-update-api.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to update
     * @param options       the update options (doc, script, etc.)
     * @param resultHandler result handler callback
     */
    void update(String index, String type, String id, UpdateOptions options, Handler<AsyncResult<UpdateResponse>> resultHandler);

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to update
     * @param resultHandler result handler callback
     */
    @GenIgnore
    @ProxyIgnore
    default void get(String index, String type, String id, Handler<AsyncResult<GetResponse>> resultHandler) {
        get(index, type, id, new GetOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/get.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to update
     * @param options       the update options
     * @param resultHandler result handler callback
     */
    void get(String index, String type, String id, GetOptions options, Handler<AsyncResult<GetResponse>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void search(String index, Handler<AsyncResult<SearchResponse>> resultHandler) {
        search(index, new SearchOptions(), resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void search(String index, SearchOptions options, Handler<AsyncResult<SearchResponse>> resultHandler) {
        search(Collections.singletonList(index), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void search(List<String> indices, Handler<AsyncResult<SearchResponse>> resultHandler) {
        search(indices, new SearchOptions(), resultHandler);
    }

    void search(List<String> indices, SearchOptions options, Handler<AsyncResult<SearchResponse>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void searchScroll(String scrollId, Handler<AsyncResult<SearchResponse>> resultHandler) {
        searchScroll(scrollId, new SearchScrollOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/reference/1.4/search-request-scroll.html
     *
     * @param scrollId
     * @param options
     * @param resultHandler
     */
    void searchScroll(String scrollId, SearchScrollOptions options, Handler<AsyncResult<SearchResponse>> resultHandler);

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to delete
     * @param resultHandler result handler callback
     */
    @GenIgnore
    @ProxyIgnore
    default void delete(String index, String type, String id, Handler<AsyncResult<DeleteResponse>> resultHandler) {
        delete(index, type, id, new DeleteOptions(), resultHandler);
    }

    /**
     * http://www.elastic.co/guide/en/elasticsearch/client/java-api/1.4/delete.html
     *
     * @param index         the index name
     * @param type          the type name
     * @param id            the source id to delete
     * @param options       optional delete options (timeout, etc.)
     * @param resultHandler result handler callback
     */
    void delete(String index, String type, String id, DeleteOptions options, Handler<AsyncResult<DeleteResponse>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void bulkIndex(final List<BulkIndexOptions> bulkIndexOptions, BulkOptions options, Handler<AsyncResult<BulkResponse>> resultHandler) {
        bulk(bulkIndexOptions, Collections.emptyList(), Collections.emptyList(), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void bulkUpdate(final List<BulkUpdateOptions> bulkUpdateOptions, BulkOptions options, Handler<AsyncResult<BulkResponse>> resultHandler) {
        bulk(Collections.emptyList(), bulkUpdateOptions, Collections.emptyList(), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void bulkDelete(final List<BulkDeleteOptions> bulkDeleteOptions, BulkOptions options, Handler<AsyncResult<BulkResponse>> resultHandler) {
        bulk(Collections.emptyList(), Collections.emptyList(), bulkDeleteOptions, options, resultHandler);
    }

    void bulk(final List<BulkIndexOptions> bulkIndexOptions,
              final List<BulkUpdateOptions> bulkUpdateOptions,
              final List<BulkDeleteOptions> bulkDeleteOptions,
              final BulkOptions bulkOptions,
              final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.BulkResponse>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions, final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.MultiSearchResponse>> resultHandler) {
        multiSearch(multiSearchQueryOptions, new MultiSearchOptions(), resultHandler);
    }

    void multiSearch(final List<MultiSearchQueryOptions> multiSearchQueryOptions,
                     final MultiSearchOptions options,
                     final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.MultiSearchResponse>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void multiGet(final List<MultiGetQueryOptions> multiGetQueryOptions, final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.MultiGetResponse>> resultHandler) {
        multiGet(multiGetQueryOptions, new MultiGetOptions(), resultHandler);
    }

    void multiGet(final List<MultiGetQueryOptions> multiGetQueryOptions,
                  final MultiGetOptions options,
                  final Handler<AsyncResult<com.hubrick.vertx.elasticsearch.model.MultiGetResponse>> resultHandler);

    @GenIgnore
    @ProxyIgnore
    default void deleteByQuery(String index, DeleteByQueryOptions options, Handler<AsyncResult<DeleteByQueryResponse>> resultHandler) {
        deleteByQuery(Collections.singletonList(index), options, resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void deleteByQuery(String index, Handler<AsyncResult<DeleteByQueryResponse>> resultHandler) {
        deleteByQuery(Collections.singletonList(index), new DeleteByQueryOptions(), resultHandler);
    }

    @GenIgnore
    @ProxyIgnore
    default void deleteByQuery(List<String> indices, Handler<AsyncResult<DeleteByQueryResponse>> resultHandler) {
        deleteByQuery(indices, new DeleteByQueryOptions(), resultHandler);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
     *
     * @param indices       the index names
     * @param options       delete by query options (timeout, etc.)
     * @param resultHandler result handler callback
     */
    void deleteByQuery(List<String> indices, DeleteByQueryOptions options, Handler<AsyncResult<DeleteByQueryResponse>> resultHandler);
}
