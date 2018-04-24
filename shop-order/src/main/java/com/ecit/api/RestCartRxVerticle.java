package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.ICartService;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
public class RestCartRxVerticle extends RestAPIRxVerticle {

    private static final Logger LOGGER = LogManager.getLogger(RestCartRxVerticle.class);
    private static final String HTTP_CART_SERVICE = "http_cart_service_api";
    private final ICartService cartService;

    public RestCartRxVerticle(ICartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());
        // API route handler
        router.post("/insertCart").handler(context -> this.requireLogin(context, this::insertCartHandler));
        router.get("/findCartPage").handler(context -> this.requireLogin(context, this::findCartPageHandler));
        router.post("/removeCart").handler(context -> this.requireLogin(context, this::removeCartHandler));
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("cart.http.address", "localhost");
        int port = config().getInteger("cart.http.port", 8085);

        // create HTTP server and publish REST service
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
    private void insertCartHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        this.checkUser(context, userId);
        final JsonObject params = context.getBodyAsJson();
        if (Objects.isNull(params)) {
            LOGGER.error("商品购物信息为空");
            this.returnWithFailureMessage(context, "商品购物信息为空");
            return;
        }
        cartService.insertCart(userId, params, handler -> {
            if (handler.failed()) {
                LOGGER.error("添加购物信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "加入购物车失败");
                return;
            }
            this.returnWithSuccessMessage(context, "成功加入购物车");
        });
    }

    /**
     * 查询购物车
     * @param context
     * @param principal
     */
    private void findCartPageHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        this.checkUser(context, userId);
        final int pageSize = Integer.parseInt(Optional.ofNullable(context.request().getParam("pageSize")).orElse("0"));
        final int page = Integer.parseInt(Optional.ofNullable(context.request().getParam("page")).orElse("0"));
        Future<Long> future = Future.future();
        cartService.findCartRowNum(userId, future);
        future.compose(rowNum -> {
            cartService.findCartPage(userId, pageSize, page, handler -> {
                if (handler.failed()) {
                    LOGGER.error("查询购物信息失败：", handler.cause());
                    this.returnWithFailureMessage(context, "查询购物车失败");
                    return;
                }
                this.returnWithSuccessMessage(context, "查询购物车成功", rowNum.intValue(), handler.result(), page);
            });
            return Future.succeededFuture();
        }).setHandler(handler -> {
            if (handler.failed()) {
                LOGGER.error("查询购物信息失败：", handler.cause());
                this.returnWithFailureMessage(context, "查询购物车失败");
                return;
            }
        });

    }

    /**
     *
     * @param context
     * @param principal
     */
    private void removeCartHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        this.checkUser(context, userId);
        final JsonObject params = context.getBodyAsJson();
        if (Objects.isNull(params)) {
            LOGGER.error("商品购物信息为空");
            this.returnWithFailureMessage(context, "商品购物信息为空");
            return;
        }
        cartService.removeCart(userId, Stream.of(StringUtils.split(params.getString("ids"), ",")).collect(Collectors.toList()),
                handler -> {
                    if (handler.failed()) {
                        LOGGER.error("删除购物信息失败：", handler.cause());
                        this.returnWithFailureMessage(context, "删除购物车失败");
                        return;
                    } else {
                        MongoClientUpdateResult updateResult = handler.result();
                        if(Objects.isNull(updateResult) || 0 == updateResult.getDocModified()){
                            LOGGER.error("删除购物信息失败：{}", updateResult.toJson());
                            this.returnWithFailureMessage(context, "删除购物车失败");
                            return;
                        }
                        this.returnWithSuccessMessage(context, "删除购物车成功");
                    }
        });
    }

    private void checkUser(RoutingContext context, long userId){
        if (Objects.isNull(userId)) {
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "用户登录信息不存在");
            return;
        }
    }
}
