package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.constants.OrderSql;
import com.ecit.enums.OrderStatus;
import com.ecit.handler.*;
import com.google.common.collect.Lists;
import com.hazelcast.internal.util.CollectionUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.data.Money;
import io.vertx.reactivex.core.Promise;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by shwang on 2018/4/5.
 */
public class OrderHandler extends JdbcRxRepositoryWrapper implements IOrderHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderHandler.class);
    private static final String ORDER_COLLECTION = "order";
    private final MongoClient mongoClient;
    private ICommodityHandler commodityHandler;
    private ICartHandler cartHandler;
    private IOrderHandler orderHandler;
    private IAddressHandler addressHandler;

    public OrderHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
        this.commodityHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).build(ICommodityHandler.class);
        this.cartHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICartHandler.CART_SERVICE_ADDRESS).build(ICartHandler.class);
        this.orderHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IOrderHandler.ORDER_SERVICE_ADDRESS).build(IOrderHandler.class);
        this.addressHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IAddressHandler.ADDRESS_SERVICE_ADDRESS).build(IAddressHandler.class);
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
    public IOrderHandler insertOrder(long orderId, long userId, String price, String freight, long shippingInformationId, String leaveMessage,
                                     JsonArray orderDetails, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addLong(orderId)
                .addLong(userId)
                .addLong(shippingInformationId)
                .addInteger(OrderStatus.VALID.getValue())
                .addString(leaveMessage)
                .addJsonArray(orderDetails)
                .addValue(new Money(Double.valueOf(price)))
                .addValue(new Money(Double.valueOf(freight)))
                , OrderSql.INSERT_ORDER_SQL).subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public IOrderHandler insertOrderHandler(String token, JsonObject params, Handler<AsyncResult<Long>> handler) {
        final long orderId = IdBuilder.getUniqueId();
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Long> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            List<BigDecimal> totalPrice = Lists.newArrayList();
            List<BigDecimal> totalFreight = Lists.newArrayList();
            JsonArray orderDetails = params.getJsonArray("order_details");
            return CompositeFuture.all(this.checkCommodity(commodityHandler, orderDetails))
                    .compose(check -> {
                        List<JsonObject> list = check.list();
                        BigDecimal freight = new BigDecimal(0);
                        for (int i = 0; i < list.size(); i++) {
                            JsonObject commodity = list.get(i);
                            if (freight.compareTo(new BigDecimal(commodity.getString("freight"))) == -1){
                                freight = new BigDecimal(commodity.getString("freight"));
                            }
                            totalPrice.add(new BigDecimal(commodity.getString("price")).multiply(new BigDecimal(orderDetails.getJsonObject(i).getInteger("order_num"))));
                        }
                        totalPrice.add(freight);
                        totalFreight.add(freight);
                        return CompositeFuture.all(this.preparedDecrCommodity(commodityHandler, orderId, orderDetails
                                //, "0.0.0.0",
                                , params.getString("ip"),
                                params.getString("logistics"), params.getString("pay_way")));
                    }).compose(msg ->{
                        Promise<Integer> orderPromise = Promise.promise();
                        orderHandler.insertOrder(orderId, userId, totalPrice.stream().reduce(BigDecimal::add).orElse(new BigDecimal("0.00")).toString(),
                                CollectionUtil.isEmpty(totalFreight) ? "0.00" : totalFreight.get(0).toString(), Long.parseLong(params.getString("shipping_information_id")),
                                params.getString("leave_message"), orderDetails, orderPromise.getDelegate());
                        return orderPromise.future();
                    }).compose(order -> {
                        if(Objects.isNull(order) || order <= 0){
                            return Future.failedFuture("订单创建失败");
                        }
                        /**
                         * 删除购物车记录
                         */
                        if(StringUtils.equals("cart", params.getString("source"))){
                            List<Long> ids = Lists.newArrayList();
                            for (int i = 0; i < orderDetails.size(); i++) {
                                ids.add(orderDetails.getJsonObject(i).getLong("id"));
                            }
                            cartHandler.removeCartByCommodityId(userId, ids, c -> {});
                        }
                        return Future.succeededFuture(orderId);
                    });
        });
        resultFuture.onComplete(handler);
        return this;
    }

    /**
     * 分页查询
     * @param handler
     * @return
     */
    @Override
    public IOrderHandler findPageOrder(String token, JsonObject params, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<List<JsonObject>> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            final Integer status = params.containsKey("status") ? params.getInteger("status") : null;
            final int page = Optional.ofNullable(params.getInteger("page")).orElse(1);
            final int pageSize = Optional.ofNullable(params.getInteger("pageSize")).orElse(10);
            Promise<List<JsonObject>> promise = Promise.promise();
            this.retrieveByPage(Objects.nonNull(status) ? Tuple.tuple().addLong(userId).addInteger(status) : Tuple.tuple().addLong(userId), pageSize, page,
                    Objects.nonNull(status) ? OrderSql.FIND_PAGE_ORDER_SQL : OrderSql.FIND_ALL_PAGE_ORDER_SQL)
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IOrderHandler findOrderRowNum(String token, JsonObject params, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<JsonObject> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Integer status = params.containsKey("status") ? params.getInteger("status") : null;
            Promise<JsonObject> promise = Promise.promise();
            this.retrieveOne(Objects.nonNull(status) ? Tuple.tuple().addLong(userId).addInteger(status) : Tuple.tuple().addLong(userId),
                    Objects.nonNull(status) ? OrderSql.FIND_ORDER_ROWNUM_SQL : OrderSql.FIND_ALL_ORDER_ROWNUM_SQL)
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IOrderHandler preparedInsertOrder(String token, JsonArray params, Handler<AsyncResult<String>> handler) {
        Promise<String> promise = Promise.promise();
        mongoClient.rxInsert(ORDER_COLLECTION, new JsonObject().put("order", params))
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 查询预
     * @param id
     * @param handler
     * @return
     */
    @Override
    public IOrderHandler findPreparedOrder(String id, Handler<AsyncResult<List<JsonObject>>> handler) {
        Promise<JsonObject> promise = Promise.promise();
        mongoClient.rxFindOne(ORDER_COLLECTION, new JsonObject().put("_id", id), null).subscribe(promise::complete, promise::fail);
        promise.future().compose(preparedOrder -> {

            final JsonArray orderArray = preparedOrder.getJsonArray("order");
            List<Long> ids = Lists.newArrayList();
            for (int i = 0; i < orderArray.size(); i++) {
                JsonObject order = orderArray.getJsonObject(i);
                ids.add(Long.parseLong(order.getString("order_id")));
            }
            Promise<List<JsonObject>> commodifyPromise = Promise.promise();
            commodityHandler.findCommodityByIds(ids, commodifyHandler -> {
                if (commodifyHandler.failed()){
                    commodifyPromise.fail(commodifyHandler.cause());
                }
                List<JsonObject> commodityList = commodifyHandler.result();
                commodityList.stream().forEach(commodity -> {
                    for (int i = 0; i < orderArray.size(); i++) {
                        JsonObject order = orderArray.getJsonObject(i);
                        if(Long.parseLong(order.getString("order_id")) == Long.parseLong(commodity.getString("commodity_id"))){
                            commodity.put("order_num", order.getInteger("order_num"));
                            commodity.put("source", order.getString("source"));
                        }
                    }
                });
                commodifyPromise.complete(commodityList);
            });
            return commodifyPromise.future();
        }).onComplete(handler);
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
    public IOrderHandler getOrderById(long orderId, long userId, Handler<AsyncResult<JsonObject>> handler) {
        Promise<JsonObject> promise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addLong(orderId).addLong(userId), OrderSql.FIND_ORDER_BY_ID)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public IOrderHandler getOrderByIdHandler(String token, long orderId, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<JsonObject> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            Promise<JsonObject> orderPromise = Promise.promise();
            orderHandler.getOrderByIdHandler(token, orderId, orderPromise.getDelegate());
            return orderPromise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    /**
     * 付款成功，修改订单状态
     * @param orderId
     * @param versions
     * @return
     */
    @Override
    public IOrderHandler payOrder(long orderId, int versions, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        LOGGER.info("payOrder:{}", orderId);
        this.execute(Tuple.tuple().addInteger(OrderStatus.PAY.getValue()).addLong(orderId).addInteger(versions), OrderSql.PAY_ORDER_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 退货
     * @param orderId
     * @param handler
     * @return
     */
    @Override
    public IOrderHandler refundHandler(String token, long orderId, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<JsonObject> orderPromise = Promise.promise();
            this.getOrderById(orderId, userId, orderPromise.getDelegate());
            return orderPromise.future().compose(order -> {
                if(Objects.isNull(order)){
                    return Future.failedFuture("order_not_found");
                }
                if(order.getInteger("order_status").intValue() != OrderStatus.PAY.getValue() &&
                        order.getInteger("order_status").intValue() != OrderStatus.SHIP.getValue()){
                    return Future.failedFuture("order_status_error");
                }
                Promise<Integer> promise = Promise.promise();
                pgPool.rxGetConnection().flatMap(conn -> conn.preparedQuery(OrderSql.CANCEL_ORDER_SQL)
                                .rxExecute(Tuple.tuple().addInteger(OrderStatus.AUDIT_REFUND.getValue()).addLong(orderId).addInteger(order.getInteger("versions")))
                                .flatMap(updateResult -> conn.preparedQuery(OrderSql.REFUND_SQL)
                                        .rxExecute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(orderId).addInteger(params.getInteger("refund_type"))
                                                .addString(params.getString("refund_reason"))
                                                .addString(params.getString("refund_money")).addString(params.getString("refund_description")).addLong(userId)))
                                .doAfterTerminate(conn::close)
                ).subscribe(rs -> promise.complete(rs.rowCount()), promise::fail);
                return promise.future();
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IOrderHandler undoRefund(long orderId, long userId, Handler<AsyncResult<Integer>> handler) {
        Promise<JsonObject> orderPromise = Promise.promise();
        this.getOrderById(orderId, userId, orderPromise.getDelegate());
        orderPromise.future().compose(order -> {
            if(Objects.isNull(order)){
                return Future.failedFuture("order_not_found");
            }
            if(order.getInteger("order_status").intValue() != OrderStatus.AUDIT_REFUND.getValue()){
                return Future.failedFuture("order_status_error");
            }
            Promise<Integer> promise = Promise.promise();
            pgPool.rxGetConnection().flatMap(conn -> conn.preparedQuery(OrderSql.CANCEL_REFUND_SQL)
                            .rxExecute(Tuple.tuple().addInteger(OrderStatus.AUDIT_REFUND.getValue()).addLong(orderId).addInteger(order.getInteger("versions")))
                            .flatMap(updateResult -> conn.preparedQuery(OrderSql.DELETE_REFUND_SQL)
                                            .rxExecute(Tuple.tuple().addLong(orderId)))
                            .doAfterTerminate(conn::close)
            ).subscribe(rs -> promise.complete(rs.rowCount()), promise::fail);
            return promise.future();
        }).onComplete(handler);
        return this;
    }

    @Override
    public IOrderHandler getAddressHandler(String token, long orderId, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<JsonObject> orderFuture = Promise.promise();
            orderHandler.getOrderById(orderId, userId, orderFuture.getDelegate());
            return orderFuture.future().compose(order -> {
                if(Objects.isNull(order)){
                    LOGGER.error("获取订单【{}】信息失败！", orderId);
                    return Future.failedFuture("获取订单信息失败！");
                }
                Promise<JsonObject> addressPromise = Promise.promise();
                addressHandler.getAddressById(order.getLong("shipping_information_id"), addressPromise.getDelegate());
                return addressPromise.future().compose(address -> Future.succeededFuture(orderFuture.future().result().put("information", address)));
            });
        }).onComplete(handler);
        return this;
    }

    /**
     * 检查商品数量是否足够
     * @param commodityService
     * @param orderDetails
     * @return
     */
    private List<Future> checkCommodity(ICommodityHandler commodityService, JsonArray orderDetails){
        final List<Future> isOk = new ArrayList<>(orderDetails.size());
        for (int i = 0; i < orderDetails.size(); i++) {
            Promise promise = Promise.promise();
            isOk.add(promise.future());
            JsonObject order = orderDetails.getJsonObject(i);
            commodityService.findCommodityById(order.getLong("id"), orderHandler -> {
                if(orderHandler.succeeded()){
                    JsonObject commodity = orderHandler.result();
                    if(order.getInteger("order_num") > commodity.getInteger("num")){
                        promise.fail("订单数量大于库存数据量");
                        return ;
                    }
                    order.put("commodity_name", commodity.getString("commodity_name"));
                    order.put("price", commodity.getString("price"));
                    order.put("image_url", commodity.getString("image_url"));
                    promise.complete(commodity);
                } else {
                    LOGGER.info("调用商品【{}】详情接口失败", order.getLong("id"));
                    promise.fail(orderHandler.cause());
                }
            });
        }
        return isOk;
    }

    /**
     * 预扣商品库存
     * @param commodityService
     * @param orderId
     * @param orderDetails
     * @return
     */
    private List<Future> preparedDecrCommodity(ICommodityHandler commodityService, long orderId, JsonArray orderDetails, String ip, String logistics, String payWay) {
        final List<Future> isOk = new ArrayList<>(orderDetails.size());
        for (int i = 0; i < orderDetails.size(); i++) {
            Promise promise = Promise.promise();
            isOk.add(promise.future());
            JsonObject order = orderDetails.getJsonObject(i);
            commodityService.preparedCommodity(order.getLong("id"), orderId, order.getInteger("order_num"), ip, logistics, payWay,
                    hander -> {
                        if(hander.succeeded()){
                            LOGGER.info("商品【{}】预扣商品库存成功", order.getLong("id"));
                            promise.complete();
                        } else {
                            LOGGER.info("调用商品【{}】预处理接口失败", order.getLong("id"));
                            promise.fail(hander.cause());
                        }
                    });
        }
        return isOk;
    }
}
