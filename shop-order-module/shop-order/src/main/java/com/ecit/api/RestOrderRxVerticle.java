package com.ecit.api;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.constants.Constants;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.IpUtils;
import com.ecit.common.utils.JsonUtils;
import com.ecit.handler.IOrderHandler;
import com.google.common.collect.Lists;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

/**
 * Created by shwang on 2018/4/5.
 */
public class RestOrderRxVerticle extends RestAPIRxVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RestOrderRxVerticle.class);
    private static final String HTTP_ORDER_SERVICE = "http_order_service_api";
    private IOrderHandler orderHandler;

    @Override
    public void start() throws Exception {
        super.start();
        this.orderHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IOrderHandler.ORDER_SERVICE_ADDRESS).build(IOrderHandler.class);
        final RestRefundRxVerticle restRefundRxVerticle = new RestRefundRxVerticle(orderHandler);
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler

        /**
         * 登录拦截
         */
        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        router.post("/insertOrder").handler(this::insertOrderHandler);
        router.post("/preparedInsertOrder").handler(this::preparedInsertOrderHandler);
        router.post("/findOrder").handler(this::findOrderHandler);
        router.get("/findPreparedOrder/:orderId").handler(this::findPreparedOrderHandler);
        router.get("/getOrder/:orderId").handler(this::getOrderHandler);
        router.get("/getAddress/:orderId").handler(this::getAddressHandler);
        router.put("/refund/:orderId").handler(restRefundRxVerticle::refundHandler);
        router.put("/undoRefund/:orderId").handler(restRefundRxVerticle::undoRefundHandler);
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("order.http.address", "localhost");
        int port = config().getInteger("order.http.port", 8084);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_ORDER_SERVICE, host, port, "order.api.name").subscribe();
            LOGGER.info("shop-order server started!");
        }, error -> {
            LOGGER.info("shop-order server start fail!", error);
        });
    }

    /**
     * 下单
     *
     * @param context 上下文
     */
    private void insertOrderHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonObject params = context.getBodyAsJson();
        params.put("ip", IpUtils.getIpAddr(context.request()));
        JsonArray orderDetails = params.getJsonArray("order_details");
        if (Objects.isNull(params) || Objects.isNull(orderDetails) || orderDetails.isEmpty()) {
            LOGGER.error("订单信息参数错误:{}", params);
            this.returnWithFailureMessage(context, "下单失败！");
            return;
        }
        orderHandler.insertOrderHandler(token, params, handler -> {
            if (handler.failed()) {
                LOGGER.error("下单失败: ", handler.cause());
                this.returnWithFailureMessage(context, "下单失败！");
                return ;
            }
            LOGGER.info("用户下单成功！");
            this.returnWithSuccessMessage(context, "下单成功！", handler.result() + "");
            return ;
        });
    }

    /**
     * 订单预处理
     * @param context
     */
    private void preparedInsertOrderHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonArray orderParams = context.getBodyAsJsonArray();
        orderHandler.preparedInsertOrder(token, orderParams, handler -> {
            if (handler.failed()) {
                LOGGER.error("预下单失败！",handler.cause());
                this.returnWithFailureMessage(context, "下单失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "处理成功", handler.result());
            return ;
        });
    }

    /**
     * 根据用户信息查询订单信息
     *
     * @param context
     */
    private void findOrderHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonObject params = context.getBodyAsJson();
        if (Objects.isNull(params)) {
            LOGGER.error("查询订单信息参数错误:{}", params);
            this.returnWithFailureMessage(context, "查询订单信息失败！");
            return;
        }

        Promise<JsonObject> promise = Promise.promise();
        orderHandler.findOrderRowNum(token, params, promise);
        final int page = Optional.ofNullable(params.getInteger("page")).orElse(1);
        promise.future().compose(rowNum -> {
            if(JsonUtils.isNull(rowNum) || rowNum.getInteger("rownum") <= 0){
                this.returnWithSuccessMessage(context, "查询订单信息成功！", rowNum.getInteger("rownum"), Lists.newArrayList(), page);
                return Future.succeededFuture();
            }
            orderHandler.findPageOrder(token, params, handler -> {
                if (handler.succeeded()) {
                    this.returnWithSuccessMessage(context, "查询订单信息成功！", rowNum.getInteger("rownum"), handler.result(), page);
                } else {
                    LOGGER.error("查询订单信息失败: ", handler.cause());
                    this.returnWithFailureMessage(context, "查询订单信息失败！");
                }
            });
            return Future.succeededFuture();
        }).onComplete(end -> {
            if (end.failed()) {
                LOGGER.error("查询订单信息失败: ", end.cause());
                this.returnWithFailureMessage(context, "查询订单信息失败！");
                return ;
            }
        });
    }

    /**
     * 查询预下单信息
     * @param context
     */
    private void findPreparedOrderHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final String orderId = context.request().getParam("orderId");
        orderHandler.findPreparedOrder(orderId, handler -> {
            if (handler.failed()){
                LOGGER.error("查询预下单失败！", handler.cause());
                this.returnWithFailureMessage(context, "操作异常");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询成功", handler.result());
            return ;
        });
    }

    /**
     *
     * @param context
     */
    private void getOrderHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final long orderId = Long.parseLong(context.pathParam("orderId"));
        orderHandler.getOrderByIdHandler(token, orderId, handler -> {
            if (handler.failed()) {
                LOGGER.error("查询订单信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询订单信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询订单信息成功！", handler.result());
        });
    }

    private void getAddressHandler(RoutingContext context) {
        final String token = context.request().getHeader(Constants.TOKEN);
        final long orderId = Long.parseLong(context.pathParam("orderId"));
        orderHandler.getAddressHandler(token, orderId, handler -> {
            if (handler.failed()) {
                LOGGER.error("查询订单信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询订单信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询订单信息成功！", handler.result());
        });
    }

}
