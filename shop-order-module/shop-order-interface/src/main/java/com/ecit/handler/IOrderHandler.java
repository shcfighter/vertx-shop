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
    IOrderHandler findPageOrder(long userId, Integer status, int size, int offset, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IOrderHandler findOrderRowNum(long userId, Integer status, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler preparedInsertOrder(JsonArray params, Handler<AsyncResult<String>> handler);

    @Fluent
    IOrderHandler findPreparedOrder(String id, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler getOrderById(long orderId, long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IOrderHandler payOrder(long orderId, int versions, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IOrderHandler refund(long orderId, long userId, int refundType, String refundReason, String refundMoney, String refundDescription, Handler<AsyncResult<UpdateResult>> handler);

    @Fluent
    IOrderHandler undoRefund(long orderId, long userId, Handler<AsyncResult<UpdateResult>> handler);
}
