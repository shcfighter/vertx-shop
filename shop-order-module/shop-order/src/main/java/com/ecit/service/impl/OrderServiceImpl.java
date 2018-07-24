package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.constants.OrderSql;
import com.ecit.enums.OrderStatus;
import com.ecit.service.IOrderService;
import com.ecit.service.IdBuilder;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;

import java.util.List;
import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
public class OrderServiceImpl extends JdbcRxRepositoryWrapper implements IOrderService {

    private static final String ORDER_COLLECTION = "order";
    private final MongoClient mongoClient;

    public OrderServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
        mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
    }

    /**
     * 下单
     * @param userId
     * @param shippingInformationId
     * @param leaveMessage
     * @param orderDetails
     * @return
     */
    @Override
    public IOrderService insertOrder(long orderId, long userId, String price, String freight, long shippingInformationId, String leaveMessage,
                                     JsonArray orderDetails, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(orderId)
                .add(userId)
                .add(shippingInformationId)
                .add(OrderStatus.VALID.getValue())
                .add(leaveMessage)
                .add(orderDetails.encodePrettily())
                .add(price)
                .add(freight)
                , OrderSql.INSERT_ORDER_SQL).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 分页查询
     * @param userId
     * @param size
     * @param page
     * @param handler
     * @return
     */
    @Override
    public IOrderService findPageOrder(long userId, Integer status, int size, int page, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> future = Future.future();
        this.retrieveByPage(Objects.nonNull(status) ? new JsonArray().add(userId).add(status) : new JsonArray().add(userId), size, page,
                Objects.nonNull(status) ? OrderSql.FIND_PAGE_ORDER_SQL : OrderSql.FIND_ALL_PAGE_ORDER_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IOrderService findOrderRowNum(long userId, Integer status, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(Objects.nonNull(status) ? new JsonArray().add(userId).add(status) : new JsonArray().add(userId),
                Objects.nonNull(status) ? OrderSql.FIND_ORDER_ROWNUM_SQL : OrderSql.FIND_ALL_ORDER_ROWNUM_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IOrderService preparedInsertOrder(JsonArray params, Handler<AsyncResult<String>> handler) {
        Future<String> future = Future.future();
        mongoClient.rxInsert(ORDER_COLLECTION, new JsonObject().put("order", params))
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 查询预
     * @param id
     * @param handler
     * @return
     */
    @Override
    public IOrderService findPreparedOrder(String id, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        mongoClient.rxFindOne(ORDER_COLLECTION, new JsonObject().put("_id", id), null)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public IOrderService getOrderById(long orderId, long userId, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(orderId).add(userId), OrderSql.FIND_ORDER_BY_ID)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 付款成功，修改订单状态
     * @param orderId
     * @param versions
     * @return
     */
    @Override
    public IOrderService payOrder(long orderId, int versions, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(OrderStatus.PAY.getValue()).add(orderId).add(versions), OrderSql.PAY_ORDER_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 退货
     * @param orderId
     * @param userId
     * @param refundType
     * @param refundReason
     * @param refundMoney
     * @param handler
     * @return
     */
    @Override
    public IOrderService refund(long orderId, long userId, int refundType, String refundReason, String refundMoney, String refundDescription, Handler<AsyncResult<UpdateResult>> handler) {
        Future<JsonObject> orderFuture = Future.future();
        this.getOrderById(orderId, userId, orderFuture);
        orderFuture.compose(order -> {
           if(Objects.isNull(order)){
               return Future.failedFuture("order_not_found");
           }
           if(order.getInteger("order_status").intValue() != OrderStatus.PAY.getValue() &&
                   order.getInteger("order_status").intValue() != OrderStatus.SHIP.getValue()){
                return Future.failedFuture("order_status_error");
           }
           Future<UpdateResult> future = Future.future();
            postgreSQLClient.rxGetConnection().flatMap(conn ->
                    conn.rxSetAutoCommit(false).toSingleDefault(false)
                            .flatMap(autoCommit -> conn.rxUpdateWithParams(OrderSql.CANCEL_ORDER_SQL,
                                    new JsonArray().add(OrderStatus.AUDIT_REFUND.getValue()).add(orderId).add(order.getInteger("versions"))))
                            .flatMap(updateResult -> conn.rxUpdateWithParams(OrderSql.REFUND_SQL,
                                    new JsonArray().add(IdBuilder.getUniqueId()).add(orderId).add(refundType).add(refundReason)
                                            .add(refundMoney).add(refundDescription).add(userId)))
                            // Rollback if any failed with exception propagation
                            .onErrorResumeNext(ex -> conn.rxRollback()
                                    .toSingleDefault(true)
                                    .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                                    .flatMap(ignore -> Single.error(ex))
                            )
                            // close the connection regardless succeeded or failed
                            .doAfterTerminate(conn::close)
            ).subscribe(future::complete, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }

    @Override
    public IOrderService undoRefund(long orderId, long userId, Handler<AsyncResult<UpdateResult>> handler) {
        Future<JsonObject> orderFuture = Future.future();
        this.getOrderById(orderId, userId, orderFuture);
        orderFuture.compose(order -> {
            if(Objects.isNull(order)){
                return Future.failedFuture("order_not_found");
            }
            if(order.getInteger("order_status").intValue() != OrderStatus.AUDIT_REFUND.getValue()){
                return Future.failedFuture("order_status_error");
            }
            Future<UpdateResult> future = Future.future();
            postgreSQLClient.rxGetConnection().flatMap(conn ->
                    conn.rxSetAutoCommit(false).toSingleDefault(false)
                            .flatMap(autoCommit -> conn.rxUpdateWithParams(OrderSql.CANCEL_REFUND_SQL,
                                    new JsonArray().add(OrderStatus.AUDIT_REFUND.getValue()).add(orderId).add(order.getInteger("versions"))))
                            .flatMap(updateResult -> conn.rxUpdateWithParams(OrderSql.DELETE_REFUND_SQL,
                                    new JsonArray().add(orderId)))
                            // Rollback if any failed with exception propagation
                            .onErrorResumeNext(ex -> conn.rxRollback()
                                    .toSingleDefault(true)
                                    .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                                    .flatMap(ignore -> Single.error(ex))
                            )
                            // close the connection regardless succeeded or failed
                            .doAfterTerminate(conn::close)
            ).subscribe(future::complete, future::fail);
            return future;
        }).setHandler(handler);
        return this;
    }
}
