package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.ICartService;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        router.get("/insertOrder/:id").handler(this::insertOrderHandler);
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("order.http.address", "localhost");
        int port = config().getInteger("order.http.port", 8085);

        // create HTTP server and publish REST service
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_CART_SERVICE, host, port).subscribe();
            LOGGER.info("shop-order server started!");
        }, error -> {
            LOGGER.info("shop-order server start fail!", error);
        });
    }

    /**
     * 下单
     * @param context 上下文
     */
    private void insertOrderHandler(RoutingContext context){
    }
}
