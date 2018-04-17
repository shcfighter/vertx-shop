package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
@VertxGen
@ProxyGen
public interface ICartService {

    String CART_SERVICE_ADDRESS = "cart_service_address";

    @Fluent
    ICartService insertCart(long userId, JsonObject params, Handler<AsyncResult<String>> handler);

    @Fluent
    ICartService findCartPage(long userId, int pageSize, int page, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    ICartService findCartRowNum(long userId, Handler<AsyncResult<Long>> handler);

    @Fluent
    ICartService removeCart(long userId, List<String> ids, Handler<AsyncResult<MongoClientUpdateResult>> handler);
}
