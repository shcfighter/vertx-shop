package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.MustacheUtils;
import com.ecit.constants.CommoditySql;
import com.ecit.service.ICommodityService;
import com.ecit.service.IdBuilder;
import com.google.common.collect.Lists;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.*;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by za-wangshenhua on 2018/3/15.
 */

public class CommodityServiceImpl extends JdbcRxRepositoryWrapper implements ICommodityService {

    private static final Logger LOGGER = LogManager.getLogger(CommodityServiceImpl.class);
    /**
     * es商品索引indeces
     */
    private static final String SHOP_INDICES = "shop";

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
    public ICommodityService searchCommodity(String keyword, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler) {
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
                .setFetchSource(true).setSize(pageSize).setFrom(this.calcPage(page, pageSize))
                .addAggregation(new AggregationOption().setName("brand_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "brand_name").put("size", 5)))
                .addAggregation(new AggregationOption().setName("category_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "category_name").put("size", 5)));
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
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(id), CommoditySql.FIND_COMMODITY_BY_ID)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICommodityService findCommodityByIds(List<Long> ids, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> future = Future.future();
        JsonArray params = new JsonArray();
        List<String> buffer = Lists.newArrayList();
        ids.forEach(id -> {
            buffer.add("?");
            params.add(id);
        });
        this.retrieveMany(params,
                MustacheUtils.mustacheString(CommoditySql.FIND_COMMODITY_BY_IDS, Map.of("ids",buffer.stream().collect(Collectors.joining(",")))))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICommodityService findCommodityFromEsById(long id, Handler<AsyncResult<SearchResponse>> handler) {
        Future<SearchResponse> future = Future.future();
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
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICommodityService findCommodityBySalesVolume(Handler<AsyncResult<SearchResponse>> handler) {
        Future<SearchResponse> future = Future.future();
        final SearchOptions searchOptions = new SearchOptions()
                .setQuery(new JsonObject("{\"match_all\":{}}"))
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setSize(3)
                .addFieldSort("month_sales_volume", SortOrder.DESC);
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICommodityService findBrandCategory(String keyword, Handler<AsyncResult<SearchResponse>> handler) {
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
                .setFetchSource(true).setSize(0)
                .addAggregation(new AggregationOption().setName("brand_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "brand_name").put("size", 10)))
                .addAggregation(new AggregationOption().setName("category_name")
                        .setType(AggregationOption.AggregationType.TERMS)
                        .setDefinition(new JsonObject().put("field", "category_name").put("size", 10)));
        rxElasticSearchService.search(SHOP_INDICES, searchOptions)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
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
    public ICommodityService preparedCommodity(long id, long orderId, int num, String ip, String logistics, String payWay, Handler<AsyncResult<UpdateResult>> handler) {
        Future<UpdateResult> future = Future.future();
        postgreSQLClient.rxGetConnection()
            .flatMap(conn ->
                conn.rxSetAutoCommit(false).toSingleDefault(false)
                    .flatMap(autoCommit -> conn.rxUpdateWithParams(CommoditySql.ORDER_COMMODITY_BY_ID, new JsonArray().add(num). add(num).add(id)))
                    .flatMap(updateResult -> conn.rxUpdateWithParams(CommoditySql.ORDER_LOG_SQL, new JsonArray()
                        .add(IdBuilder.getUniqueId()).add(orderId).add(id).add(ip).add(num).add(logistics).add(payWay)))
                    // Rollback if any failed with exception propagation
                    .onErrorResumeNext(ex -> conn.rxRollback()
                        .toSingleDefault(true)
                        .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                        .flatMap(ignore -> Single.error(ex))
                    )
                    // close the connection regardless succeeded or failed
                    .doAfterTerminate(conn::close)
            ).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
