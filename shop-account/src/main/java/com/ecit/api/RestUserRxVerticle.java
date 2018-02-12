package com.ecit.api;

import com.ecit.UserVerticle;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.IUserService;
import com.ecit.service.impl.UserServiceImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestUserRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestUserRxVerticle.class);
    private static final String HTTP_LOGIN_USER_SERVICE = "http_login_user_service_api";
    private IUserService userService;

    public static void main(String[] args) {
        Config cfg = new Config();
        GroupConfig group = new GroupConfig();
        group.setName("p-dev");
        group.setPassword("p-dev");
        cfg.setGroupConfig(group);
        // 申明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(RestUserRxVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("api.name", "user")
                        .put("host", "localhost")
                        .put("port", 5432)
                        .put("maxPoolSize", 50)
                        .put("username", "postgres")
                        .put("password", "123456")
                        .put("database", "vertx_shop")
                        .put("charset", "UTF-8")
                        .put("queryTimeout", 10000)
                )));
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post("/login").handler(this::loginHandler);
        this.enableCorsSupport(router);
        userService = new UserServiceImpl(vertx, config());

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

    public void loginHandler(RoutingContext routingContext){
        JsonObject params = routingContext.getBodyAsJson();
        userService.login(params.getString("loginName"),
                params.getString("pwd"),
                handler -> {
                    if(handler.succeeded()){
                        if(null != handler.result()) {
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
