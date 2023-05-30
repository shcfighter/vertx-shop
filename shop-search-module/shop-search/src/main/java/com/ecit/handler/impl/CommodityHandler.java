package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.MustacheUtils;
import com.ecit.constants.CommoditySql;
import com.ecit.handler.ICommodityHandler;
import com.ecit.handler.IdBuilder;
import com.google.common.collect.Lists;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.sqlclient.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by shwang on 2018/3/15.
 */

public class CommodityHandler extends JdbcRxRepositoryWrapper implements ICommodityHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommodityHandler.class);
    /**
     * es商品索引indeces
     */
    private static final String SHOP_INDICES = "shop";

    final RxElasticSearchService rxElasticSearchService;

    public CommodityHandler(Vertx vertx, JsonObject config) {
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
    public ICommodityHandler searchCommodity(String keyword, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler) {
        Promise<SearchResponse> promise = Promise.promise();
        JsonObject searchJson = null;
        if (StringUtils.isBlank(keyword)) {
            searchJson = new JsonObject("{\"match_all\": {}}");
        } else {
            searchJson = new JsonObject("{\n" +
                "    \"multi_match\" : {\n" +
                "      \"query\":    \"" + keyword + "\",\n" +
                "      \"fields\": [ \"commodity_name\", \"brand_name\", \"category_name\", \"remarks\", \"description\", \"large_class\" ] \n" +
                "    }\n" +
                "}");
        }
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(searchJson)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true).setSize(pageSize).setFrom(this.calcPage(page, pageSize))
                .addAggregation(new AggregationOption().setName("brand_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "brand_name").put("size", 5)))
                .addAggregation(new AggregationOption().setName("category_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "category_name").put("size", 5)))
                //.addFieldSort("commodity_id", SortOrder.DESC)
                .addScripSort("Math.random()", ScriptSortOption.Type.NUMBER, new JsonObject(), SortOrder.DESC); //随机排序
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler searchLargeClassCommodity(String keyword, Handler<AsyncResult<SearchResponse>> handler) {
        Promise<SearchResponse> promise = Promise.promise();
        JsonObject searchJson = null;
        if (StringUtils.isBlank(keyword)) {
            searchJson = new JsonObject("{\"match_all\": {}}");
        } else {
            searchJson = new JsonObject("{\n" +
                    "    \"multi_match\" : {\n" +
                    "      \"query\":    \"" + keyword + "\",\n" +
                    "      \"fields\": [ \"brand_name\", \"category_name\", \"large_class\" ] \n" +
                    "    }\n" +
                    "}");
        }
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(searchJson)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true).setSize(7)
                //.addFieldSort("commodity_id", SortOrder.DESC)
                .addScripSort("Math.random()", ScriptSortOption.Type.NUMBER, new JsonObject(), SortOrder.DESC); //随机排序
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 商品id查询详细信息
     * @param id
     * @param handler
     * @return
     */
    @Override
    public ICommodityHandler findCommodityById(long id, Handler<AsyncResult<JsonObject>> handler) {
        Promise<JsonObject> promise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addLong(id), CommoditySql.FIND_COMMODITY_BY_ID)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findCommodityByIds(List<Long> ids, Handler<AsyncResult<List<JsonObject>>> handler) {
        Promise<List<JsonObject>> promise = Promise.promise();
        Tuple params = Tuple.tuple();
        List<String> buffer = Lists.newArrayList();
        ids.forEach(id -> {
            buffer.add("?");
            params.addLong(id);
        });
        this.retrieveMany(params,
                MustacheUtils.mustacheString(CommoditySql.FIND_COMMODITY_BY_IDS, Map.of("ids",buffer.stream().collect(Collectors.joining(",")))))
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findCommodityFromEsById(long id, Handler<AsyncResult<SearchResponse>> handler) {
        Promise<SearchResponse> promise = Promise.promise();
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(new JsonObject("{" +
                        "       \"match\":{" +
                        "           \"commodity_id\": \"" + id + "\"" +
                        "       }" +
                        "}"))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setSize(1);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 推测喜欢的商品
     * @param keywords
     * @return
     */
    @Override
    public ICommodityHandler preferencesCommodity(List<String> keywords, Handler<AsyncResult<SearchResponse>> handler) {
        Promise<SearchResponse> promise = Promise.promise();
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(new JsonObject("{\n" +
                        "      \"bool\": {\n" +
                        "         \"should\": [\n" +
                        keywords.stream().map(key -> {
                            return "{\"multi_match\":{\"query\":\"" + key + "\",\"fields\":[\"commodity_name\",\"brand_name\",\"category_name\",\"remarks\",\"description\"]}}";
                        }).collect(Collectors.joining(",")) +
                        "         ]\n" +
                        "      }\n" +
                        "}\n"))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setSize(3);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findCommodityBySalesVolume(Handler<AsyncResult<SearchResponse>> handler) {
        Promise<SearchResponse> promise = Promise.promise();
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(new JsonObject("{\"match_all\":{}}"))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setSize(3)
                .addFieldSort("month_sales_volume", SortOrder.DESC);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findBrandCategory(String keyword, Handler<AsyncResult<SearchResponse>> handler) {
        Promise<SearchResponse> promise = Promise.promise();
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
                .setFetchSource(true).setSize(0)
                .addAggregation(new AggregationOption().setName("brand_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "brand_name").put("size", 10)))
                .addAggregation(new AggregationOption().setName("category_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "category_name").put("size", 10)));
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 订单预处理
     * @param id
     * @param orderId
     * @param num
     * @param handler
     * @return
     */
    @Override
    public ICommodityHandler preparedCommodity(long id, long orderId, int num, String ip, String logistics, String payWay, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        pgPool.rxGetConnection()
            .flatMap(conn -> conn.preparedQuery(CommoditySql.ORDER_COMMODITY_BY_ID).rxExecute(Tuple.tuple().addInteger(num). addInteger(num).addLong(id))
                    .flatMap(updateResult -> conn.preparedQuery(CommoditySql.ORDER_LOG_SQL)
                            .rxExecute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(orderId).addLong(id).addString(ip).addInteger(num).addString(logistics).addString(payWay)))
                    .doAfterTerminate(conn::close)
            ).subscribe(rs -> promise.complete(rs.rowCount()), promise::fail);
        promise.future().onComplete(handler);
        return this;
    }
}
