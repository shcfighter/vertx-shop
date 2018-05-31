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
    IOrderService insertOrder(long orderId, long userId, String price, String freight, long shippingInformationId, String leaveMessage, JsonArray orderDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IOrderService findPageOrder(long userId, Integer status, int size, int offset, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IOrderService findOrderRowNum(long userId, Integer status, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderService preparedInsertOrder(JsonArray params, Handler<AsyncResult<String>> handler);

    @Fluent
    IOrderService findPreparedOrder(String id, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderService getOrderById(long orderId, long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderService payOrder(long orderId, int versions, Handler<AsyncResult<Integer>> handler);
}
