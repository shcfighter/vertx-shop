package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.IpUtils;
import com.ecit.handler.*;
import com.google.common.collect.Lists;
import com.hazelcast.util.CollectionUtil;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        // API route handler
        router.post("/insertOrder").handler(context -> this.requireLogin(context, this::insertOrderHandler));
        router.post("/preparedInsertOrder").handler(context -> this.requireLogin(context, this::preparedInsertOrderHandler));
        router.post("/findOrder").handler(context -> this.requireLogin(context, this::findOrderHandler));
        router.get("/findPreparedOrder/:orderId").handler(context -> this.requireLogin(context, this::findPreparedOrderHandler));
        router.get("/getOrder/:orderId").handler(context -> this.requireLogin(context, this::getOrderHandler));
        router.get("/getAddress/:orderId").handler(context -> this.requireLogin(context, this::getAddressHandler));
        /*router.put("/refund/:orderId").handler(context -> this.requireLogin(context, this::refundHandler));
        router.put("/undoRefund/:orderId").handler(context -> this.requireLogin(context, this::undoRefundHandler));*/
        vertx.getDelegate().deployVerticle(new RestRefundRxVerticle(orderHandler, router));
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

        final ICommodityHandler commodityService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).build(ICommodityHandler.class);
        final ICartHandler cartService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICartHandler.CART_SERVICE_ADDRESS).build(ICartHandler.class);
        final long orderId = IdBuilder.getUniqueId();
        List<BigDecimal> totalPrice = Lists.newArrayList();
        List<BigDecimal> totalFreight = Lists.newArrayList();
        CompositeFuture.all(this.checkCommodity(commodityService, orderDetails))
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
                    return CompositeFuture.all(this.preparedDecrCommodity(commodityService, orderId, orderDetails
                            //, "0.0.0.0",
                            , IpUtils.getIpAddr(context.request().getDelegate()),
                            params.getString("logistics"), params.getString("pay_way")));
                })
                .compose(msg ->{
                    Future<Integer> orderFuture = Future.future();
                    orderHandler.insertOrder(orderId, userId, totalPrice.stream().reduce(BigDecimal::add).orElse(new BigDecimal("0.00")).toString(),
                            CollectionUtil.isEmpty(totalFreight) ? "0.00" : totalFreight.get(0).toString(), Long.parseLong(params.getString("shipping_information_id")),
                            params.getString("leave_message"), orderDetails, orderFuture);
                    return orderFuture;
        }).setHandler(orderHandler -> {
            if (orderHandler.succeeded()) {
                LOGGER.info("用户【{}】下单成功！", userId);
                /**
                 * 删除购物车记录
                 */
                if(StringUtils.equals("cart", params.getString("source"))){
                    List<Long> ids = Lists.newArrayList();
                    for (int i = 0; i < orderDetails.size(); i++) {
                        ids.add(orderDetails.getJsonObject(i).getLong("id"));
                    }
                    cartService.removeCartByCommodityId(userId, ids, c -> {});
                }
                this.returnWithSuccessMessage(context, "下单成功！", orderId + "");
            } else {
                LOGGER.error("下单失败: ", orderHandler.cause());
                this.returnWithFailureMessage(context, "下单失败！");
            }
        });
    }

    /**
     * 订单预处理
     * @param context
     * @param principal
     */
    private void preparedInsertOrderHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final JsonArray orderParams = context.getBodyAsJsonArray();
        orderHandler.preparedInsertOrder(orderParams, handler -> {
            if (handler.failed()) {
                LOGGER.error("预下单失败！",handler.cause());
                this.returnWithFailureMessage(context, "下单失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "处理成功", handler.result());
        });
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
            Future future = Future.future();
            isOk.add(future);
            JsonObject order = orderDetails.getJsonObject(i);
            commodityService.preparedCommodity(order.getLong("id"), orderId, order.getInteger("order_num"), ip, logistics, payWay,
                    hander -> {
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
    private List<Future> checkCommodity(ICommodityHandler commodityService, JsonArray orderDetails){
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
                    future.complete(commodity);
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
        final int page = Optional.ofNullable(params.getInteger("page")).orElse(1);
        final int pageSize = Optional.ofNullable(params.getInteger("pageSize")).orElse(10);
        Future<JsonObject> future = Future.future();
        orderHandler.findOrderRowNum(userId, params.containsKey("status") ? params.getInteger("status") : null, future);
        future.compose(rowNum -> {
            orderHandler.findPageOrder(userId, params.containsKey("status") ? params.getInteger("status") : null, pageSize, page,
                    handler -> {
                        if (handler.succeeded()) {
                            this.returnWithSuccessMessage(context, "查询订单信息成功！", rowNum.getInteger("rownum"), handler.result(), page);
                        } else {
                            LOGGER.error("查询订单信息失败: ", handler.cause());
                            this.returnWithFailureMessage(context, "查询订单信息失败！");
                        }
                    });
            return Future.succeededFuture();
        }).setHandler(end -> {
            if (end.failed()) {
                LOGGER.error("查询订单信息失败: ", end.cause());
                this.returnWithFailureMessage(context, "查询订单信息失败！");
            }
        });
    }

    /**
     * 查询预下单信息
     * @param context
     * @param principal
     */
    private void findPreparedOrderHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final String orderId = context.request().getParam("orderId");
        Future<JsonObject> future = Future.future();
        orderHandler.findPreparedOrder(orderId, future);
        future.compose(preparedOrder -> {
            Future<List<JsonObject>> commodifyFuture = Future.future();
            final ICommodityHandler commodityService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).build(ICommodityHandler.class);
            final JsonArray orderArray = preparedOrder.getJsonArray("order");
            List<Long> ids = Lists.newArrayList();
            for (int i = 0; i < orderArray.size(); i++) {
                JsonObject order = orderArray.getJsonObject(i);
                ids.add(Long.parseLong(order.getString("order_id")));
            }
            commodityService.findCommodityByIds(ids, commodifyHandler -> {
                if (commodifyFuture.failed()){
                    LOGGER.error("查询预下单失败！", commodifyFuture.cause());
                    this.returnWithFailureMessage(context, "操作异常");
                    return ;
                }
                List<JsonObject> commodityList = commodifyHandler.result();
                try {
                    commodityList.stream().forEach(commodity -> {
                        for (int i = 0; i < orderArray.size(); i++) {
                            JsonObject order = orderArray.getJsonObject(i);
                            if(Long.parseLong(order.getString("order_id")) == Long.parseLong(commodity.getString("commodity_id"))){
                                commodity.put("order_num", order.getInteger("order_num"));
                                commodity.put("source", order.getString("source"));
                            }
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("下单失败：", e);
                    this.returnWithFailureMessage(context, "下单失败");
                    return ;
                }
                this.returnWithSuccessMessage(context, "查询成功", commodityList);
            });
            return Future.succeededFuture();
        }).setHandler(end -> {
            if (end.failed()) {
                LOGGER.error("", end.cause());
                this.returnWithFailureMessage(context, "下单失败");
            }
        });
    }

    /**
     *
     * @param context
     * @param principal
     */
    private void getOrderHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final String orderId = context.pathParam("orderId");
        orderHandler.getOrderById(Long.parseLong(orderId), userId, handler -> {
            if (handler.failed()) {
                LOGGER.error("查询订单信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询订单信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询订单信息成功！", handler.result());
        });
    }

    private void getAddressHandler(RoutingContext context, JsonObject principal) {
        final Long userId = principal.getLong("userId");
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
        final String orderId = context.pathParam("orderId");
        Future<JsonObject> orderFuture = Future.future();
        orderHandler.getOrderById(Long.parseLong(orderId), userId, orderFuture);
        orderFuture.compose(order -> {
            if(Objects.isNull(order)){
                LOGGER.error("获取订单【{}】信息失败！", orderId);
                return Future.failedFuture("获取订单信息失败！");
            }
            final IAddressHandler addressService = new ServiceProxyBuilder(vertx.getDelegate())
                    .setAddress(IAddressHandler.ADDRESS_SERVICE_ADDRESS).build(IAddressHandler.class);
            Future<JsonObject> addressFuture = Future.future();
            addressService.getAddressById(order.getLong("shipping_information_id"), addressFuture);
            return addressFuture;
        }).setHandler(handler -> {
            if (handler.failed()) {
                LOGGER.error("查询订单信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询订单信息失败！");
                return ;
            }
            JsonObject orderJson = orderFuture.result().put("information", handler.result());
            this.returnWithSuccessMessage(context, "查询订单信息成功！", orderJson);
        });
    }

    /**
     * 退货
     * @param context
     * @param principal
     */
    /*private void refundHandler(RoutingContext context, JsonObject principal) {
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

    *//**
     * 取消退货
     * @param context
     * @param principal
     *//*
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
    }*/

}
