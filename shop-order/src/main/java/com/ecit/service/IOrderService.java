package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
@VertxGen
@ProxyGen
public interface IOrderService {

    String ORDER_SERVICE_ADDRESS = "order_service_address";

    @Fluent
    IOrderService insertOrder(long userId, long shippingInformationId, String leaveMessage, JsonArray orderDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IOrderService findPageOrder(long userId, Integer status, int size, int offset, Handler<AsyncResult<List<JsonObject>>> handler);
}
