package com.ecit.api;

import com.ecit.common.constants.Constants;
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

    public RestRefundRxVerticle(IOrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    /**
     * 退货
     * @param context
     */
    protected void refundHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final long orderId = Long.parseLong(context.pathParam("orderId"));
        final JsonObject params = context.getBodyAsJson();
        orderHandler.refundHandler(token, orderId, params, handler -> {
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
     */
    protected void undoRefundHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final long orderId = Long.parseLong(context.pathParam("orderId"));
        final JsonObject params = context.getBodyAsJson();
        Future<UpdateResult> future = Future.future();
        orderHandler.refundHandler(token, orderId, params, handler -> {
            if (handler.failed()) {
                LOGGER.error("退货申请提交失败：", handler.cause());
                this.returnWithFailureMessage(context, "退货申请提交失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "退货申请提交成功！");
        });
    }

}
