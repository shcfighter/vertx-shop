package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.IUserService;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestUserVerticle extends RestAPIRxVerticle{
    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.get("/get").handler(this::apiGet);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("product.http.address", "0.0.0.0");
        int port = config().getInteger("product.http.port", 8080);

        // create HTTP server and publish REST service
        createHttpServer(router, host, port).subscribe(server -> {
            System.out.println("server started!");
        });
    }

    public void apiGet(RoutingContext routingContext){
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IUserService.USER_SERVICE_ADDRESS);
        IUserService userService = builder.build(IUserService.class);
        userService.get(handler ->
                routingContext.response().setStatusCode(200).putHeader("content-type", "application/json").end(handler.result().encodePrettily()));
    }
}
