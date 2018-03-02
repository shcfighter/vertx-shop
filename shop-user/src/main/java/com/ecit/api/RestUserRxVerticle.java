package com.ecit.api;

import com.ecit.common.enmu.RegisterType;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.service.IUserService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

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
        router.put("/changepwd").handler(context -> this.requireLogin(context, this::changePwdHandler));

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
     * @param context
     */
    private void registerHandler(RoutingContext context){
        HttpServerRequest request = context.request();
        if(!StringUtils.equals(request.getParam("password"), request.getParam("passwordConfirm"))){
            LOGGER.info("注册密码和确认密码不一致！");
            this.returnWithFailureMessage(context, "密码和确认密码不一致");
            return ;
        }
        //todo 如果为手机注册验证手机验证码

        final String salt = hashStrategy.generateSalt();
        final String type = Optional.ofNullable(request.getParam("type")).orElse(RegisterType.loginName.name());
        userService.register(StringUtils.equals(type, RegisterType.loginName.name()) ? request.getParam("loginName") : null,
                StringUtils.equals(type, RegisterType.mobile.name()) ? request.getParam("loginName") : null,
                StringUtils.equals(type, RegisterType.email.name()) ? request.getParam("loginName") : null,
                hashStrategy.computeHash(request.getParam("password"), salt, -1),
                salt,
                handler -> {
                    if(handler.succeeded()){
                        if(handler.result() >= 1) {
                            this.Ok(context, ResultItems.getReturnItemsSuccess("注册成功"));
                        } else {
                            this.Ok(context, ResultItems.getReturnItemsFailure("注册失败"));
                        }
                    } else {
                        this.internalError(context, handler.cause());
                    }
                });
    }

    /**
     * 修改密码
     * @param context
     */
    private void changePwdHandler(RoutingContext context, JsonObject principal){
        Long userId = principal.getLong("userId");
        System.out.println(userId);
        if(Objects.isNull(userId) ){
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "登录id【" + userId + "】不存在");
            return ;
        }
        userService.getMemberById(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "修改密码失败!");
                return ;
            }
            JsonObject user = handler.result();

            /**
             * 密码加密
             */
            String password = hashStrategy.computeHash(context.request().getParam("pwd"), user.getString("salt"), -1);
            userService.changePwd(userId, password,
                    user.getLong("versions"), handler2 -> {
                        if(handler2.failed()){
                            LOGGER.error("修改密码失败", handler2.cause());
                            this.returnWithFailureMessage(context, "修改密码失败!");
                            return ;
                        }
                        this.Ok(context, ResultItems.getReturnItemsSuccess("修改密码成功"));
                    });
        });
    }

}
