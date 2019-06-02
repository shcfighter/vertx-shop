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
    ICartHandler insertCartHandler(String token, JsonObject params, Handler<AsyncResult<Void>> handler);

    @Fluent
    ICartHandler findCartPage(String token, int pageSize, int page, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICartHandler findCartRowNum(String token, Handler<AsyncResult<Long>> handler);

    @Fluent
    ICartHandler removeCartHandler(String token, List<String> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler);

    @Fluent
    ICartHandler removeCartByCommodityId(long userId, List<Long> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler);
}
