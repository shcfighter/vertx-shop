package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import com.ecit.constants.OrderSql;
import com.ecit.enums.OrderStatus;
import com.ecit.service.ICommodityService;
import com.ecit.service.IOrderService;
import com.ecit.service.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;

import java.util.List;
import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
public class OrderServiceImpl extends JdbcRepositoryWrapper implements IOrderService {

    public OrderServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx.getDelegate(), config);
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
    public IOrderService insertOrder(long orderId, long userId, long shippingInformationId, String leaveMessage,
                                     JsonArray orderDetails, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(orderId)
                .add(userId)
                .add(shippingInformationId)
                .add(OrderStatus.VALID.getValue())
                .add(leaveMessage)
                .add(orderDetails.encodePrettily())
                , OrderSql.INSERT_ORDER_SQL, future);
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
                Objects.nonNull(status) ? OrderSql.FIND_PAGE_ORDER_SQL : OrderSql.FIND_ALL_PAGE_ORDER_SQL, future);
        future.setHandler(handler);
        return this;
    }
}
