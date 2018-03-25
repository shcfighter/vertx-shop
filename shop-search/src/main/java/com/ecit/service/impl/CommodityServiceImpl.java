package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import com.ecit.constants.CommoditySql;
import com.ecit.service.ICommodityService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.SearchOptions;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import com.hubrick.vertx.elasticsearch.model.SearchType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Created by za-wangshenhua on 2018/3/15.
 */

public class CommodityServiceImpl extends JdbcRepositoryWrapper implements ICommodityService {

    /**
     * es商品索引indeces
     */
    private static final String SHOP_INDICES = "shop";
    final IndexOptions indexOptions = new IndexOptions();

    final RxElasticSearchService rxElasticSearchService;

    public CommodityServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
        rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx.getDelegate(), config.getString("address"));
    }

    /**
     * 通过关键字搜索商品
     * @param keyword
     * @param handler
     * @return
     */
    @Override
    public ICommodityService searchCommodity(String keyword, Handler<AsyncResult<SearchResponse>> handler) {
        Future<SearchResponse> future = Future.future();
        JsonObject searchJson = null;
        if (StringUtils.isBlank(keyword)) {
            searchJson = new JsonObject("{\"match_all\": {}}");
        } else {
            searchJson = new JsonObject("{\n" +
                "    \"multi_match\" : {\n" +
                "      \"query\":    \"" + keyword + "\",\n" +
                "      \"fields\": [ \"commodity_name\", \"brand_name\", \"category_name\", \"remarks\", \"description\" ] \n" +
                "    }\n" +
                "}");
        }
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(searchJson)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true).setSize(12);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 商品id查询详细信息
     * @param id
     * @param handler
     * @return
     */
    @Override
    public ICommodityService findCommodityById(long id, Handler<AsyncResult<JsonObject>> handler) {
        Future future = Future.future();
        this.retrieveOne(new JsonArray().add(id), CommoditySql.FIND_COMMODITY_BY_ID)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 推测喜欢的商品
     * @param keywords
     * @return
     */
    @Override
    public ICommodityService preferencesCommodity(List<String> keywords, Handler<AsyncResult<SearchResponse>> handler) {
        Future<SearchResponse> future = Future.future();
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(new JsonObject("{\n" +
                        "   \"query\": {\n" +
                        "      \"bool\": {\n" +
                        "         \"should\": [\n" +
                        "            {\n" +
                        "               \"multi_match\": {\n" +
                        "                  \"query\": \"三星\",\n" +
                        "                  \"fields\": [\"commodity_name\",\"brand_name\",\"category_name\",\"remarks\",\"description\"]\n" +
                        "               }\n" +
                        "            },\n" +
                        "            {\n" +
                        "               \"multi_match\": {\n" +
                        "                  \"query\": \"小米\",\n" +
                        "                  \"fields\": [\"commodity_name\",\"brand_name\",\"category_name\",\"remarks\",\"description\"]\n" +
                        "               }\n" +
                        "            }\n" +
                        "         ]\n" +
                        "      }\n" +
                        "   }\n" +
                        "}\n"))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
