package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.handler.IOrderHandler;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Created by shwang on 2018/4/5.
 */
public class RestRefundRxVerticle extends RestAPIRxVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RestRefundRxVerticle.class);
    private final IOrderHandler orderHandler;
    private final Router router;

    public RestRefundRxVerticle(IOrderHandler orderHandler, Router router) {
        this.orderHandler = orderHandler;
        this.router = router;
    }

    @Override
    public void start() throws Exception {
        super.start();
        // API route handler
        router.put("/refund/:orderId").handler(context -> this.requireLogin(context, this::refundHandler));
        router.put("/undoRefund/:orderId").handler(context -> this.requireLogin(context, this::undoRefundHandler));

    }

    /**
     * 退货
     * @param context
     * @param principal
     */
    private void refundHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final String orderId = context.pathParam("orderId");
        final JsonObject params = context.getBodyAsJson();
        Future<UpdateResult> future = Future.future();
        orderHandler.refund(Long.parseLong(orderId), userId, params.getInteger("refund_type"), params.getString("refund_reason"),
                params.getString("refund_money"), params.getString("refund_description"), future);
        future.setHandler(handler -> {
            if (handler.failed()) {
                LOGGER.error("退货申请提交失败：", handler.cause());
                this.returnWithFailureMessage(context, "退货申请提交失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "退货申请提交成功！");
        });
    }

    /**
     * 取消退货
     * @param context
     * @param principal
     */
    private void undoRefundHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final String orderId = context.pathParam("orderId");
        final JsonObject params = context.getBodyAsJson();
        Future<UpdateResult> future = Future.future();
        orderHandler.refund(Long.parseLong(orderId), userId, params.getInteger("refund_type"), params.getString("refund_reason"),
                params.getString("refund_money"), params.getString("refund_description"), future);
        future.setHandler(handler -> {
            if (handler.failed()) {
                LOGGER.error("退货申请提交失败：", handler.cause());
                this.returnWithFailureMessage(context, "退货申请提交失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "退货申请提交成功！");
        });
    }

}
