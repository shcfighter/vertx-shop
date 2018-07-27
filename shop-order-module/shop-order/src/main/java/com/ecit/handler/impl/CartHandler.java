package com.ecit.handler.impl;

import com.ecit.handler.ICartHandler;
import com.ecit.handler.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;

/**
 * Created by shwang on 2018/4/6.
 */
public class CartHandler implements ICartHandler {

    private static final Logger LOGGER = LogManager.getLogger(CartHandler.class);
    /**
     * mongodb collection
     */
    private static final String CART_COLLECTION = "cart";
    final MongoClient mongoClient;

    public CartHandler(Vertx vertx, JsonObject config) {
        mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
    }

    /**
     * 加入购物车
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public ICartHandler insertCart(long userId, JsonObject params, Handler<AsyncResult<String>> handler) {
        Future<String> future = Future.future();
        mongoClient.rxInsert(CART_COLLECTION, params.put("cart_id", IdBuilder.getUniqueId())
                .put("user_id", userId)
                .put("create_time", new Date().getTime())
                .put("is_deleted", 0))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 分页查询购物车
     * @param userId
     * @param pageSize
     * @param page
     * @param handler
     * @return
     */
    @Override
    public ICartHandler findCartPage(long userId, int pageSize, int page, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> future = Future.future();
        mongoClient.rxFindWithOptions(CART_COLLECTION, new JsonObject()
                        .put("user_id", userId)
                        .put("is_deleted", 0),
                new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                        .setSort(new JsonObject().put("create_time", -1)))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 统计个数
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public ICartHandler findCartRowNum(long userId, Handler<AsyncResult<Long>> handler) {
        Future<Long> future = Future.future();
        mongoClient.rxCount(CART_COLLECTION, new JsonObject()
                        .put("user_id", userId)
                        .put("is_deleted", 0))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 根据id删除购物车
     * @param ids
     * @param handler
     * @return
     */
    @Override
    public ICartHandler removeCart(long userId, List<String> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        Future<MongoClientUpdateResult> future = Future.future();
        mongoClient.rxUpdateCollectionWithOptions(CART_COLLECTION, new JsonObject()
                        .put("user_id", userId)
                        .put("_id", new JsonObject().put("$in", new JsonArray(ids)))
                        .put("is_deleted", 0),
                new JsonObject().put("$set", new JsonObject().put("is_deleted", 1)),
                new UpdateOptions().setMulti(true))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ICartHandler removeCartByCommodityId(long userId, List<Long> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler) {
        Future<MongoClientUpdateResult> future = Future.future();
        mongoClient.rxUpdateCollectionWithOptions(CART_COLLECTION, new JsonObject()
                        .put("user_id", userId)
                        .put("commodity_id", new JsonObject().put("$in", new JsonArray(ids)))
                        .put("is_deleted", 0),
                new JsonObject().put("$set", new JsonObject().put("is_deleted", 1)),
                new UpdateOptions().setMulti(true))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
