package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.ICommodityService;
import com.ecit.service.IOrderService;
import com.ecit.service.IdBuilder;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
public class RestOrderRxVerticle extends RestAPIRxVerticle {

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
    private void insertOrderHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final JsonObject params = context.getBodyAsJson();
        JsonArray orderDetails = params.getJsonArray("order_details");
        if (Objects.isNull(params) || Objects.isNull(orderDetails) || orderDetails.isEmpty()) {
            LOGGER.error("订单信息参数错误:{}", params);
            this.returnWithFailureMessage(context, "下单失败！");
            return;
        }

        final ICommodityService commodityService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityService.SEARCH_SERVICE_ADDRESS).build(ICommodityService.class);
        final long orderId = IdBuilder.getUniqueId();
        CompositeFuture.all(this.checkCommodity(commodityService, orderDetails))
                .compose(check -> CompositeFuture.all(this.preparedDecrCommodity(commodityService, orderId, orderDetails)))
                .compose(msg ->{
            Future<Integer> orderFuture = Future.future();
            orderService.insertOrder(orderId, userId, params.getLong("shipping_information_id"),
                    params.getString("leave_message"), orderDetails, orderFuture);
            return orderFuture;
        }).setHandler(orderHandler -> {
            if (orderHandler.succeeded()) {
                LOGGER.info("用户【{}】下单成功！", userId);
                this.returnWithSuccessMessage(context, "下单成功！");
            } else {
                LOGGER.error("下单失败: ", orderHandler.cause());
                this.returnWithFailureMessage(context, "下单失败！");
            }
        });
    }

    /**
     * 预扣商品库存
     * @param commodityService
     * @param orderId
     * @param orderDetails
     * @return
     */
    private List<Future> preparedDecrCommodity(ICommodityService commodityService, long orderId, JsonArray orderDetails) {
        final List<Future> isOk = new ArrayList<>(orderDetails.size());
        for (int i = 0; i < orderDetails.size(); i++) {
            Future future = Future.future();
            isOk.add(future);
            JsonObject order = orderDetails.getJsonObject(i);
            commodityService.preparedCommodity(order.getLong("id"), orderId, order.getInteger("order_num"), hander -> {
                if(hander.succeeded()){
                    LOGGER.info("商品【{}】预扣商品库存成功", order.getLong("id"));
                    future.complete();
                } else {
                    LOGGER.info("调用商品【{}】预处理接口失败", order.getLong("id"));
                    future.fail(hander.cause());
                }
            });
        }
        return isOk;
    }

    /**
     * 检查商品数量是否足够
     * @param commodityService
     * @param orderDetails
     * @return
     */
    private List<Future> checkCommodity(ICommodityService commodityService, JsonArray orderDetails){
        final List<Future> isOk = new ArrayList<>(orderDetails.size());
        for (int i = 0; i < orderDetails.size(); i++) {
            Future future = Future.future();
            isOk.add(future);
            JsonObject order = orderDetails.getJsonObject(i);
            commodityService.findCommodityById(order.getLong("id"), orderHandler -> {
                if(orderHandler.succeeded()){
                    JsonObject commodity = orderHandler.result();
                    if(order.getInteger("order_num") > commodity.getInteger("num")){
                        future.fail("订单数量大于库存数据量");
                        return ;
                    }
                    order.put("commodity_name", commodity.getString("commodity_name"));
                    order.put("price", commodity.getString("price"));
                    order.put("image_url", commodity.getString("image_url"));
                    future.complete();
                } else {
                    LOGGER.info("调用商品【{}】详情接口失败", order.getLong("id"));
                    future.fail(orderHandler.cause());
                }
            });
        }
        return isOk;
    }

    /**
     * 根据用户信息查询订单信息
     *
     * @param context
     * @param principal
     */
    private void findOrderHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final JsonObject params = context.getBodyAsJson();
        if (Objects.isNull(params)) {
            LOGGER.error("查询订单信息参数错误:{}", params);
            this.returnWithFailureMessage(context, "查询订单信息失败！");
            return;
        }
        orderService.findPageOrder(userId, params.containsKey("status") ? params.getInteger("status") : null,
                params.getInteger("size"), params.getInteger("page"),
                handler -> {
                    if (handler.succeeded()) {
                        this.returnWithSuccessMessage(context, "查询订单信息成功！", handler.result());
                    } else {
                        LOGGER.error("查询订单信息失败: ", handler.cause());
                        this.returnWithFailureMessage(context, "查询订单信息失败！");
                    }
                });

    }

}
