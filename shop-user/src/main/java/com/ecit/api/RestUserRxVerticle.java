package com.ecit.api;

import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.IUserService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestUserRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestUserRxVerticle.class);
    private static final String HTTP_LOGIN_USER_SERVICE = "http_login_user_service_api";

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post("/register").handler(this::registerHandler);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("product.http.address", "localhost");
        int port = config().getInteger("product.http.port", 8080);

        // create HTTP server and publish REST service
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_LOGIN_USER_SERVICE, host, port).subscribe();
            LOGGER.info("shop-login server started!");
        }, error -> {
            LOGGER.info("shop-login server start fail!");
        });
    }

    public void registerHandler(RoutingContext routingContext){
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IUserService.USER_SERVICE_ADDRESS);
        IUserService userService = builder.build(IUserService.class);
        userService.register(routingContext.request().getParam("mobile"),
                routingContext.request().getParam("email"),
                routingContext.request().getParam("pwd"),
                routingContext.request().getParam("salt"),
                handler -> {
                    if(handler.succeeded()){
                        if(handler.result() >= 1) {
                            this.Ok(routingContext, ResultItems.getReturnItemsSuccess("注册成功"));
                        } else {
                            this.Ok(routingContext, ResultItems.getReturnItemsSuccess("注册失败"));
                        }
                    } else {
                        this.internalError(routingContext, handler.cause());
                    }
                });
    }

}
