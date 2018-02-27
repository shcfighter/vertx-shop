package com.ecit.api;

import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.service.IUserService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestUserRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestUserRxVerticle.class);
    private static final String HTTP_USER_SERVICE = "http_user_service_api";
    private final IUserService userService;
    private ShopHashStrategy hashStrategy;

    public RestUserRxVerticle(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post("/register").handler(this::registerHandler);
        router.put("/changepwd").handler(routingContext -> this.requireLogin(routingContext, this::changePwdHandler));

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("product.http.address", "localhost");
        int port = config().getInteger("product.http.port", 8080);

        // create HTTP server and publish REST service
        createHttpServer(router, host, port).subscribe(server -> {
            this.hashStrategy = new DefaultHashStrategy(vertx.getDelegate());
            this.publishHttpEndpoint(HTTP_USER_SERVICE, host, port).subscribe();
            LOGGER.info("shop-user server started!");
        }, error -> {
            LOGGER.info("shop-user server start fail!", error);
        });
    }

    /**
     * 注册
     * @param routingContext
     */
    private void registerHandler(RoutingContext routingContext){
        final String salt = hashStrategy.generateSalt();
        userService.register(routingContext.request().getParam("mobile"),
                routingContext.request().getParam("email"),
                hashStrategy.computeHash(routingContext.request().getParam("pwd"), salt, -1),
                salt,
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

    /**
     * 修改密码
     * @param routingContext
     */
    private void changePwdHandler(RoutingContext routingContext, JsonObject principal){
        Long userId = principal.getLong("userId");
        System.out.println(userId);
        if(Objects.isNull(userId) ){
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithMessage(routingContext, "登录id【" + userId + "】不存在");
            return ;
        }
        userService.getMemberById(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithMessage(routingContext, "修改密码失败!");
                return ;
            }
            JsonObject user = handler.result();

            /**
             * 密码加密
             */
            String password = hashStrategy.computeHash(routingContext.request().getParam("pwd"), user.getString("salt"), -1);
            userService.changePwd(userId, password,
                    user.getLong("versions"), handler2 -> {
                        if(handler2.failed()){
                            LOGGER.error("修改密码失败", handler2.cause());
                            this.returnWithMessage(routingContext, "修改密码失败!");
                            return ;
                        }
                        this.Ok(routingContext, ResultItems.getReturnItemsSuccess("修改密码成功"));
                    });
        });
    }

}
