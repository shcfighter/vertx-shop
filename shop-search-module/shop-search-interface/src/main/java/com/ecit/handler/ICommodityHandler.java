package com.ecit.handler;

import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

/**
 * Created by shwang on 2018/3/15.
 */
@VertxGen
@ProxyGen
public interface ICommodityHandler {

    String SEARCH_SERVICE_ADDRESS = "search_service_address";

    @Fluent
    ICommodityHandler searchCommodity(String keyword, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler searchLargeClassCommodity(String keyword, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler findCommodityById(long id, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    ICommodityHandler findCommodityByIds(List<Long> ids, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICommodityHandler findCommodityFromEsById(long id, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler preferencesCommodity(List<String> keywords, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler findCommodityBySalesVolume(Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler findBrandCategory(String keyword, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityHandler preparedCommodity(long id, long orderId, int num, String ip, String logistics, String payWay, Handler<AsyncResult<UpdateResult>> handler);
}
