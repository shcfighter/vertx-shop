package com.ecit.api;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.constants.Constants;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.handler.ICartHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by shwang on 2018/4/5.
 */
public class RestCartRxVerticle extends RestAPIRxVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RestCartRxVerticle.class);
    private static final String HTTP_CART_SERVICE = "http_cart_service_api";
    private ICartHandler cartHandler;

    @Override
    public void start() throws Exception {
        super.start();
        this.cartHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICartHandler.CART_SERVICE_ADDRESS).build(ICartHandler.class);
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        /**
         * 登录拦截
         */
        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        // API route handler
        router.post("/insertCart").handler(this::insertCartHandler);
        router.get("/findCartPage").handler(this::findCartPageHandler);
        router.get("/findCartRowNum").handler(this::findCartRowNumHandler);
        router.post("/removeCart").handler(this::removeCartHandler);
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("cart.http.address", "localhost");
        int port = config().getInteger("cart.http.port", 8085);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_CART_SERVICE, host, port, "cart.api.name").subscribe();
            LOGGER.info("cart-order server started!");
        }, error -> {
            LOGGER.info("cart-order server start fail!", error);
        });
    }

    /**
     * 加入购物车
     * @param context 上下文
     */
    private void insertCartHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonObject params = context.getBodyAsJson();
        if (Objects.isNull(params)) {
            LOGGER.error("商品购物信息为空");
            this.returnWithFailureMessage(context, "商品购物信息为空");
            return;
        }

        cartHandler.insertCartHandler(token, params, handler -> {
            if (handler.failed()) {
                LOGGER.error("添加购物信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "加入购物车失败");
                return;
            }
            this.returnWithSuccessMessage(context, "成功加入购物车");
            return ;
        });
    }

    /**
     * 查询购物车
     * @param context
     */
    private void findCartPageHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        final int pageSize = Integer.parseInt(Optional.ofNullable(context.request().getParam("pageSize")).orElse("0"));
        final int page = Integer.parseInt(Optional.ofNullable(context.request().getParam("page")).orElse("0"));
        Promise<Long> promise = Promise.promise();
        Promise<Void> cartPromise = Promise.promise();
        cartHandler.findCartRowNum(token, promise);
        promise.future().andThen(rowNum -> {
            cartHandler.findCartPage(token, pageSize, page, handler -> {
                if (handler.failed()) {
                    LOGGER.error("查询购物信息失败：", handler.cause());
                    this.returnWithFailureMessage(context, "查询购物车失败");
                    return;
                }
                this.returnWithSuccessMessage(context, "查询购物车成功", rowNum.result().intValue(), handler.result(), page);
            });
            cartPromise.complete();
            //return Future.succeededFuture();
        });
        cartPromise.future().andThen(handler -> {
            if (handler.failed()) {
                LOGGER.error("查询购物信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询购物车失败");
                return;
            }
        });

    }

    /**
     * 购物车商品数量
     * @param context
     */
    private void findCartRowNumHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        cartHandler.findCartRowNum(token, handler -> {
            if (handler.failed()) {
                LOGGER.error("查询购物信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询购物车失败！");
                return;
            } else {
                this.returnWithSuccessMessage(context, "查询购物车成功！", handler.result());
            }
        });

    }

    /**
     *
     * @param context
     */
    private void removeCartHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonObject params = context.getBodyAsJson();
        if (Objects.isNull(params)) {
            LOGGER.error("商品购物信息为空");
            this.returnWithFailureMessage(context, "商品购物信息为空");
            return;
        }
        cartHandler.removeCartHandler(token, Stream.of(StringUtils.split(params.getString("ids"), ",")).collect(Collectors.toList()),
                handler -> {
                    if (handler.failed()) {
                        LOGGER.error("删除购物信息失败：", handler.cause());
                        this.returnWithFailureMessage(context, "删除购物车失败");
                        return;
                    }
                    MongoClientUpdateResult updateResult = handler.result();
                    if(Objects.isNull(updateResult) || 0 == updateResult.getDocModified()){
                        LOGGER.error("删除购物信息失败：{}", updateResult.toJson());
                        this.returnWithFailureMessage(context, "删除购物车失败");
                        return;
                    }
                    this.returnWithSuccessMessage(context, "删除购物车成功");
                    return ;
        });
    }

}
