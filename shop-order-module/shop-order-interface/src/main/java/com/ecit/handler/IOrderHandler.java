package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

/**
 * Created by shwang on 2018/4/5.
 */
@VertxGen
@ProxyGen
public interface IOrderHandler {

    String ORDER_SERVICE_ADDRESS = "order_service_address";

    @Fluent
    IOrderHandler insertOrder(long orderId, long userId, String price, String freight, long shippingInformationId, String leaveMessage, JsonArray orderDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IOrderHandler insertOrderHandler(String token, JsonObject params, Handler<AsyncResult<Long>> handler);

    @Fluent
    IOrderHandler findPageOrder(String token, JsonObject params, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IOrderHandler findOrderRowNum(String token, JsonObject params, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler preparedInsertOrder(String token, JsonArray params, Handler<AsyncResult<String>> handler);

    @Fluent
    IOrderHandler findPreparedOrder(String id, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IOrderHandler getOrderById(long orderId, long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler getOrderByIdHandler(String token, long orderId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler payOrder(long orderId, int versions, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IOrderHandler refundHandler(String token, long orderId, JsonObject params, Handler<AsyncResult<UpdateResult>> handler);

    @Fluent
    IOrderHandler undoRefund(long orderId, long userId, Handler<AsyncResult<UpdateResult>> handler);

    @Fluent
    IOrderHandler getAddressHandler(String token, long orderId, Handler<AsyncResult<JsonObject>> handler);
}
