package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.MustacheUtils;
import com.ecit.constants.CommoditySql;
import com.ecit.handler.ICommodityHandler;
import com.ecit.handler.IdBuilder;
import com.ecit.model.SearchResponse;
import com.google.common.collect.Lists;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
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
    final WebClient webClient;
    final JsonObject elasticConfig;

    public CommodityHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);

        this.elasticConfig = config.getJsonObject("elasticsearch");
        WebClientOptions options = new WebClientOptions().setKeepAlive(true);
        this.webClient = WebClient.create(vertx.getDelegate(), options);
    }

    /**
     * 通过关键字搜索商品
     * @param keyword
     * @param handler
     * @return
     */
    @Override
    public ICommodityHandler searchCommodity(String keyword, int pageSize, int page, Handler<AsyncResult<SearchResponse>> handler) {
        String searchQuery = """
                {
                    "query":{
                        "match_all":{
                                
                        }
                    },
                    "size": %s,
                    "from": %s,
                    "aggs":{
                        "brand_name":{
                            "terms":{
                                "field":"brand_name",
                                "size":5
                            }
                        },
                        "category_name":{
                            "terms":{
                                "field":"category_name",
                                "size":5
                            }
                        }
                    },
                    "sort":{
                        "_script":{
                            "script":"Math.random()",
                            "type":"number",
                            "order":"asc"
                        }
                    }
                }
                """.formatted(pageSize, this.calcPage(page, pageSize));
        if (StringUtils.isNotBlank(keyword)) {
            searchQuery = """
                    {
                        "query":{
                            "multi_match":{
                                "query":"%s",
                                "fields":[
                                    "commodity_name",
                                    "brand_name",
                                    "category_name",
                                    "remarks",
                                    "description",
                                    "large_class"
                                ]
                            }
                        },
                        "size": %s,
                        "from": %s,
                        "aggs":{
                            "brand_name":{
                                "terms":{
                                    "field":"brand_name",
                                    "size":5
                                }
                            },
                            "category_name":{
                                "terms":{
                                    "field":"category_name",
                                    "size":5
                                }
                            }
                        },
                        "sort":{
                            "_script":{
                                "script":"Math.random()",
                                "type":"number",
                                "order":"asc"
                            }
                        }
                    }
                    """.formatted(keyword, pageSize, this.calcPage(page, pageSize));
        }

        Promise<SearchResponse> promise = Promise.promise();
        this.webClient
                .post(elasticConfig.getInteger("port", 9200), elasticConfig.getString("url", "127.0.0.1"), SHOP_INDICES + "/_search")
                .sendJsonObject(
                        new JsonObject(searchQuery))
                .onSuccess(res -> {
                    SearchResponse searchResponse = res.bodyAsJson(SearchResponse.class);
                    promise.complete(searchResponse);
                }).onFailure(err -> promise.fail(err));
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler searchLargeClassCommodity(String keyword, Handler<AsyncResult<SearchResponse>> handler) {
        String searchQuery = """
                {
                    "query":{
                        "match_all":{
                                
                        }
                    },
                    "size": 7,
                    "sort":{
                        "_script":{
                            "script":"Math.random()",
                            "type":"number",
                            "order":"asc"
                        }
                    }
                }
                """;
        if (StringUtils.isNotBlank(keyword)) {
            searchQuery = """
                    {
                        "query":{
                            "multi_match":{
                                "query":"%s",
                                "fields":[
                                    "commodity_name",
                                    "brand_name",
                                    "category_name",
                                    "remarks",
                                    "description",
                                    "large_class"
                                ]
                            }
                        },
                        "size": 7,
                        "sort":{
                            "_script":{
                                "script":"Math.random()",
                                "type":"number",
                                "order":"asc"
                            }
                        }
                    }
                    """.formatted(keyword);
        }
        Promise<SearchResponse> promise = Promise.promise();
        this.webClient
                .post(elasticConfig.getInteger("port", 9200), elasticConfig.getString("url", "127.0.0.1"), SHOP_INDICES + "/_search")
                .sendJsonObject(
                        new JsonObject(searchQuery))
                .onSuccess(res -> {
                    SearchResponse searchResponse = res.bodyAsJson(SearchResponse.class);
                    promise.complete(searchResponse);
                }).onFailure(err -> promise.fail(err));
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
        for (int i = 1; i <= ids.size(); i++) {
            buffer.add("$" + i);
            params.addLong(ids.get(i - 1));
        }
        this.retrieveMany(params,
                MustacheUtils.mustacheString(CommoditySql.FIND_COMMODITY_BY_IDS, Map.of("ids",buffer.stream().collect(Collectors.joining(",")))))
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findCommodityFromEsById(long id, Handler<AsyncResult<SearchResponse>> handler) {
        final String searchQuery = """
                {
                    "query":{
                        "match":{
                            "commodity_id":%s
                        }
                    },
                    "size":1
                }
                """.formatted(id);
        Promise<SearchResponse> promise = Promise.promise();
        this.webClient
                .post(elasticConfig.getInteger("port", 9200), elasticConfig.getString("url", "127.0.0.1"), SHOP_INDICES + "/_search")
                .sendJsonObject(
                        new JsonObject(searchQuery))
                .onSuccess(res -> {
                    SearchResponse searchResponse = res.bodyAsJson(SearchResponse.class);
                    promise.complete(searchResponse);
                }).onFailure(err -> promise.fail(err));
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

        String keywordJson = keywords.stream().map(key -> {
            return """
                    {
                                        "multi_match":{
                                            "query":"%s",
                                            "fields":[
                                                "commodity_name",
                                                "brand_name",
                                                "category_name",
                                                "remarks",
                                                "description",
                                                "large_class"
                                            ]
                                        }
                                    }
                    """.formatted(key);
        }).collect(Collectors.joining(","));

        String searchQuery = """
                {
                    "query":{
                        "bool":{
                            "should":[
                                %s
                            ]
                        }
                    },
                    "size":3
                }
                """.formatted(keywordJson);
        Promise<SearchResponse> promise = Promise.promise();
        this.webClient
                .post(elasticConfig.getInteger("port", 9200), elasticConfig.getString("url", "127.0.0.1"), SHOP_INDICES + "/_search")
                .sendJsonObject(
                        new JsonObject(searchQuery))
                .onSuccess(res -> {
                    SearchResponse searchResponse = res.bodyAsJson(SearchResponse.class);
                    promise.complete(searchResponse);
                }).onFailure(err -> promise.fail(err));
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findCommodityBySalesVolume(Handler<AsyncResult<SearchResponse>> handler) {
        String searchQuery = """
                {
                    "query":{
                        "match_all":{
                                
                        }
                    },
                    "size":3,
                    "sort":[
                        {
                            "month_sales_volume":{
                                "order":"desc"
                            }
                        }
                    ]
                }
                """;
        Promise<SearchResponse> promise = Promise.promise();
        this.webClient
                .post(elasticConfig.getInteger("port", 9200), elasticConfig.getString("url", "127.0.0.1"), SHOP_INDICES + "/_search")
                .sendJsonObject(
                        new JsonObject(searchQuery))
                .onSuccess(res -> {
                    SearchResponse searchResponse = res.bodyAsJson(SearchResponse.class);
                    promise.complete(searchResponse);
                }).onFailure(err -> promise.fail(err));
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public ICommodityHandler findBrandCategory(String keyword, Handler<AsyncResult<SearchResponse>> handler) {
        String searchQuery = """
                {
                    "query":{
                        "match_all":{
                                
                        }
                    },
                    "size": 0,
                    "aggs":{
                        "brand_name":{
                            "terms":{
                                "field":"brand_name",
                                "size":10                            }
                        },
                        "category_name":{
                            "terms":{
                                "field":"category_name",
                                "size":10
                            }
                        }
                    }
                }
                """;
        if (StringUtils.isNotBlank(keyword)) {
            searchQuery = """
                    {
                        "query":{
                            "multi_match":{
                                "query":"%s",
                                "fields":[
                                    "commodity_name",
                                    "brand_name",
                                    "category_name",
                                    "remarks",
                                    "description"
                                ]
                            }
                        },
                        "size": 0,
                        "aggs":{
                            "brand_name":{
                                "terms":{
                                    "field":"brand_name",
                                    "size":10
                                }
                            },
                            "category_name":{
                                "terms":{
                                    "field":"category_name",
                                    "size":10
                                }
                            }
                        }
                    }
                    """.formatted(keyword);
        }

        Promise<SearchResponse> promise = Promise.promise();
        this.webClient
                .post(elasticConfig.getInteger("port", 9200), elasticConfig.getString("url", "127.0.0.1"), SHOP_INDICES + "/_search")
                .sendJsonObject(
                        new JsonObject(searchQuery))
                .onSuccess(res -> {
                    SearchResponse searchResponse = res.bodyAsJson(SearchResponse.class);
                    promise.complete(searchResponse);
                }).onFailure(err -> promise.fail(err));
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
