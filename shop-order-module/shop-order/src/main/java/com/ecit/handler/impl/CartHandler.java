package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.handler.ICartHandler;
import com.ecit.handler.ICommodityHandler;
import com.ecit.handler.IdBuilder;
import io.reactivex.disposables.Disposable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Created by shwang on 2018/4/6.
 */
public class CartHandler extends JdbcRxRepositoryWrapper implements ICartHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartHandler.class);
    /**
     * mongodb collection
     */
    private static final String CART_COLLECTION = "cart";
    final MongoClient mongoClient;
    private ICommodityHandler commodityHandler;

    public CartHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb", new JsonObject()));
        this.commodityHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).build(ICommodityHandler.class);
    }

    /**
     * 加入购物车
     * @param token
     * @param handler
     * @return
     */
    @Override
    public ICartHandler insertCartHandler(String token, JsonObject params, Handler<AsyncResult<Void>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<Void> promise = Promise.promise();
            commodityHandler.findCommodityById(params.getLong("commodity_id"), commodityHandler -> {
                if (commodityHandler.failed()) {
                    LOGGER.error("insert cart fail, find commodity error: ", commodityHandler.cause());
                    promise.fail(commodityHandler.cause());
                    return ;
                }
                JsonObject commodity = commodityHandler.result();
                if (JsonUtils.isNull(commodity)) {
                    LOGGER.error("insert cart fail, find commodity[{}] error: ", params.getLong("commodity_id"));
                    promise.fail("commodity empty!");
                    return ;
                }
                mongoClient.findOne(CART_COLLECTION, new JsonObject()
                        .put("user_id", userId)
                        .put("commodity_id", params.getLong("commodity_id"))
                        .put("is_deleted", 0), null, cartHandler -> {
                    if (cartHandler.failed()) {
                        LOGGER.error("find cart failed: ", cartHandler.cause());
                        promise.fail(cartHandler.cause());
                        return ;
                    }
                    JsonObject cart = cartHandler.result();
                    if (JsonUtils.isNull(cart)) {
                        mongoClient.insert(CART_COLLECTION, params
                                .put("brand_name", commodity.getString("brand_name"))
                                .put("category_name", commodity.getString("category_name"))
                                .put("commodity_name", commodity.getString("commodity_name"))
                                .put("price", commodity.getString("price"))
                                .put("original_price", commodity.getString("original_price"))
                                .put("image_url", commodity.getString("image_url"))
                                .put("order_num", params.getInteger("order_num", 1))
                                .put("cart_id", IdBuilder.getUniqueId())
                                .put("user_id", userId)
                                .put("create_time", new Date().getTime())
                                .put("is_deleted", 0), h -> {
                            if (h.failed()) {
                                LOGGER.error("insert cart fail: ", h.cause());
                                promise.fail(h.cause());
                            } else {
                                promise.complete();
                            }
                        });
                    } else {
                        mongoClient.updateCollectionWithOptions(CART_COLLECTION, new JsonObject().put("user_id", userId)
                                        .put("cart_id", cart.getLong("cart_id")).put("is_deleted", 0),
                                new JsonObject().put("$set", new JsonObject().put("order_num", cart.getInteger("order_num") + params.getInteger("order_num", 1))),
                                new UpdateOptions().setMulti(true), h -> {
                            if (h.failed()) {
                                LOGGER.error("update cart failed: ", h.cause());
                                promise.fail(h.cause());
                            } else {
                                promise.complete();
                            }
                        });
                    }
                });
            });
            return promise.future();
        }).onComplete(handler);
        return this;
    }

    /**
     * 分页查询购物车
     * @param token
     * @param pageSize
     * @param page
     * @param handler
     * @return
     */
    @Override
    public ICartHandler findCartPage(String token, int pageSize, int page, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<List<JsonObject>> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<List<JsonObject>> promise = Promise.promise();
            mongoClient.rxFindWithOptions(CART_COLLECTION, new JsonObject()
                            .put("user_id", userId)
                            .put("is_deleted", 0),
                    new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                            .setSort(new JsonObject().put("create_time", -1)))
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    /**
     * 统计个数
     * @param token
     * @param handler
     * @return
     */
    @Override
    public ICartHandler findCartRowNum(String token, Handler<AsyncResult<Long>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<Long> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            JsonObject session = sessionResult.result();
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                promise.complete(0L);
            }
            final long userId = session.getLong("userId");
            mongoClient.rxCount(CART_COLLECTION, new JsonObject()
                    .put("user_id", userId)
                    .put("is_deleted", 0))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(handler);
        return this;
    }

    /**
     * 根据id删除购物车
     * @param ids
     * @param handler
     * @return
     */
    @Override
    public ICartHandler removeCartHandler(String token, List<String> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<MongoClientUpdateResult> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<MongoClientUpdateResult> promise = Promise.promise();
            mongoClient.rxUpdateCollectionWithOptions(CART_COLLECTION, new JsonObject()
                            .put("user_id", userId)
                            .put("_id", new JsonObject().put("$in", new JsonArray(ids)))
                            .put("is_deleted", 0),
                    new JsonObject().put("$set", new JsonObject().put("is_deleted", 1)),
                    new UpdateOptions().setMulti(true))
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public ICartHandler removeCartByCommodityId(long userId, List<Long> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        Promise<MongoClientUpdateResult> promise = Promise.promise();
        mongoClient.rxUpdateCollectionWithOptions(CART_COLLECTION, new JsonObject()
                        .put("user_id", userId)
                        .put("commodity_id", new JsonObject().put("$in", new JsonArray(ids)))
                        .put("is_deleted", 0),
                new JsonObject().put("$set", new JsonObject().put("is_deleted", 1)),
                new UpdateOptions().setMulti(true))
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }
}
