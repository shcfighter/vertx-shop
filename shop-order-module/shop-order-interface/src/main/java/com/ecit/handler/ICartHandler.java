package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.List;

/**
 * Created by shwang on 2018/4/5.
 */
@VertxGen
@ProxyGen
public interface ICartHandler {

    String CART_SERVICE_ADDRESS = "cart_service_address";

    @Fluent
    ICartHandler insertCart(long userId, JsonObject params, Handler<AsyncResult<String>> handler);

    @Fluent
    ICartHandler findCartPage(long userId, int pageSize, int page, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICartHandler findCartRowNum(long userId, Handler<AsyncResult<Long>> handler);

    @Fluent
    ICartHandler removeCart(long userId, List<String> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler);

    @Fluent
    ICartHandler removeCartByCommodityId(long userId, List<Long> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler);
}
