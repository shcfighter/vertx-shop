package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import com.ecit.constants.CommoditySql;
import com.ecit.service.ICommodityService;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/3/15.
 */

public class CommodityServiceImpl extends JdbcRepositoryWrapper implements ICommodityService {

    final IndexOptions indexOptions = new IndexOptions();

    final RxElasticSearchService rxElasticSearchService;
    private static final String SHOP_INDICES = "shop";

    public CommodityServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
        rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx.getDelegate(), config.getString("address"));
    }

    @Override
    public ICommodityService searchCommodity(String commodity, Handler<AsyncResult<SearchResponse>> handler) {
        Future<SearchResponse> future = Future.future();
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(new JsonObject("{\n" +
                        "    \"multi_match\" : {\n" +
                        "      \"query\":    \"" + commodity + "\",\n" +
                        "      \"fields\": [ \"commodity_name\", \"brand_name\", \"category_name\", \"remarks\", \"description\" ] \n" +
                        "    }\n" +
                        "}"))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICommodityService findCommodityById(long id, Handler<AsyncResult<JsonObject>> handler) {
        Future future = Future.future();
        this.retrieveOne(new JsonArray().add(id), CommoditySql.FIND_COMMODITY_BY_ID)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
