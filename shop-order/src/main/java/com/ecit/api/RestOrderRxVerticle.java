package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.IOrderService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
public class RestOrderRxVerticle  extends RestAPIRxVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RestOrderRxVerticle.class);
    private static final String HTTP_ORDER_SERVICE = "http_order_service_api";
    private final IOrderService orderService;

    public RestOrderRxVerticle(IOrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        // API route handler
        router.post("/insertOrder").handler(context -> this.requireLogin(context, this::insertOrderHandler));
        router.post("/findOrder").handler(context -> this.requireLogin(context, this::findOrderHandler));
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("order.http.address", "localhost");
        int port = config().getInteger("order.http.port", 8084);

        // create HTTP server and publish REST service
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_ORDER_SERVICE, host, port).subscribe();
            LOGGER.info("shop-order server started!");
        }, error -> {
            LOGGER.info("shop-order server start fail!", error);
        });
    }

    /**
     * 下单
     * @param context 上下文
     */
    private void insertOrderHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        if(Objects.isNull(userId) ){
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return ;
        }
        final JsonObject params = context.getBodyAsJson();
        JsonArray orderDetails = params.getJsonArray("orderDetails");
        if(Objects.isNull(params) || Objects.isNull(orderDetails) || orderDetails.isEmpty()){
            LOGGER.error("订单信息参数错误:{}", params);
            this.returnWithFailureMessage(context, "下单失败！");
            return ;
        }

        for (int i = 0; i < orderDetails.size(); i++) {

        }
        orderService.insertOrder(userId, params.getLong("shippingInformationId"),
                params.getString("leaveMessage"), orderDetails,
                handler -> {
                    if(handler.succeeded()){
                        LOGGER.info("用户【{}】下单成功！", userId);
                        this.returnWithSuccessMessage(context, "下单成功！");
                    } else {
                        LOGGER.error("下单失败: ", handler.cause());
                        this.returnWithFailureMessage(context, "下单失败！");
                    }
                });

    }

    /**
     *  根据用户信息查询订单信息
     * @param context
     * @param principal
     */
    private void findOrderHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        if(Objects.isNull(userId) ){
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return ;
        }
        final JsonObject params = context.getBodyAsJson();
        if(Objects.isNull(params)){
            LOGGER.error("查询订单信息参数错误:{}", params);
            this.returnWithFailureMessage(context, "查询订单信息失败！");
            return ;
        }
        orderService.findPageOrder(userId, params.containsKey("status") ? params.getInteger("status") : null,
                params.getInteger("size"), params.getInteger("page"),
                handler -> {
                    if(handler.succeeded()){
                        this.returnWithSuccessMessage(context, "查询订单信息成功！", handler.result());
                    } else {
                        LOGGER.error("查询订单信息失败: ", handler.cause());
                        this.returnWithFailureMessage(context, "查询订单信息失败！");
                    }
                });

    }
}
