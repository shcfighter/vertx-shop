package com.ecit.api;

import com.ecit.common.enmu.RegisterType;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.service.IMessageService;
import com.ecit.service.IUserService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
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
        router.get("/activate/:loginName/:code").handler(this::activateHandler);
        router.put("/changepwd").handler(context -> this.requireLogin(context, this::changePwdHandler));
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("user.http.address", "localhost");
        int port = config().getInteger("user.http.port", 8080);

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

        final String loginName = request.getParam("loginName");
        final String password = request.getParam("password");
        final String salt = hashStrategy.generateSalt();
        final String type = Optional.ofNullable(request.getParam("type")).orElse(RegisterType.loginName.name());
        //如果为手机注册验证手机验证码
        IMessageService messageService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageService.MESSAGE_SERVICE_ADDRESS).build(IMessageService.class);
        if(StringUtils.equals(type, RegisterType.mobile.name())) {
            messageService.findMessage(loginName, RegisterType.mobile, handler -> {
                if (handler.succeeded()) {
                    final JsonObject message = handler.result();
                    if (Objects.nonNull(message)
                            && StringUtils.endsWithIgnoreCase(message.getString("code"), request.getParam("code"))) {
                        this.register(context, type, loginName, salt, password);
                        messageService.updateMessage(loginName, RegisterType.mobile, deleteHandler -> {
                            if (deleteHandler.succeeded()) {
                                LOGGER.info("数据删除成功！");
                            } else {
                                LOGGER.info("数据删除失败！", deleteHandler.cause());
                            }
                        });
                    } else {
                        LOGGER.info("验证码不匹配！");
                        this.returnWithFailureMessage(context, "验证码错误");
                        return ;
                    }
                } else {
                    LOGGER.info("查询验证码失败", handler.cause());
                    this.returnWithFailureMessage(context, "验证码错误");
                    return ;
                }
            });
        } else if(StringUtils.equals(type, RegisterType.email.name())){
            this.register(context, type, loginName, salt, password);
            messageService.registerEmailMessage(loginName, emailSendMessage -> {});
        }

    }

    /**
     * 注册逻辑
     * @param context
     * @param type
     * @param loginName
     * @param salt
     * @param password
     */
    private void register(RoutingContext context, String type, String loginName, String salt, String password){
        userService.register(StringUtils.equals(type, RegisterType.loginName.name()) ? loginName : null,
                StringUtils.equals(type, RegisterType.mobile.name()) ? loginName : null,
                StringUtils.equals(type, RegisterType.email.name()) ? loginName : null,
                hashStrategy.computeHash(password, salt, -1),
                salt,
                registerHandler -> {
                    if (registerHandler.succeeded()) {
                        if (registerHandler.result() >= 1) {
                            this.Ok(context, ResultItems.getReturnItemsSuccess("注册成功"));
                        } else {
                            this.Ok(context, ResultItems.getReturnItemsFailure("注册失败"));
                        }
                    } else {
                        this.internalError(context, registerHandler.cause());
                    }
                });
    }

    private void activateHandler(RoutingContext context) {
        HttpServerRequest request = context.request();
        LOGGER.info("{}, {}", request.getParam("loginName"), request.getParam("code"));
        context.response()
                .putHeader("Location", "http://111.231.132.168/index.html")
                //.putHeader("content-type", "text/html; charset=utf-8")
                .setStatusCode(302)
                .end();
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
