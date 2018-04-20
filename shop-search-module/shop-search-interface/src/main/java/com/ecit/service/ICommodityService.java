package com.ecit.service;

import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by za-wangshenhua on 2018/3/15.
 */
@VertxGen
@ProxyGen
public interface ICommodityService {

    String SEARCH_SERVICE_ADDRESS = "search_service_address";

    @Fluent
    ICommodityService searchCommodity(String keyword, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityService findCommodityById(long id, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    ICommodityService findCommodityFromEsById(long id, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityService preferencesCommodity(List<String> keywords, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityService findCommodityBySalesVolume(Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityService findBrandCategory(String keyword, Handler<AsyncResult<SearchResponse>> handler);

    @Fluent
    ICommodityService preparedCommodity(long id, long orderId, int num, Handler<AsyncResult<Integer>> handler);
}
