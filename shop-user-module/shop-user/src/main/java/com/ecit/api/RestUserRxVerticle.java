package com.ecit.api;

import com.ecit.common.enmu.RegisterType;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.service.IMessageService;
import com.ecit.service.IUserService;
import io.vertx.core.json.JsonObject;
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
     * @param context 上下文
     */
    private void registerHandler(RoutingContext context){
        final JsonObject params = context.getBodyAsJson();
        if(!StringUtils.equals(params.getString("password"), params.getString("passwordConfirm"))){
            LOGGER.info("注册密码和确认密码不一致！");
            this.returnWithFailureMessage(context, "密码和确认密码不一致");
            return ;
        }

        final String loginName = params.getString("loginName");
        final String password = params.getString("password");
        final String salt = hashStrategy.generateSalt();
        final String type = Optional.ofNullable(params.getString("type")).orElse(RegisterType.loginName.name());
        //如果为手机注册验证手机验证码
        IMessageService messageService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageService.MESSAGE_SERVICE_ADDRESS).build(IMessageService.class);
        if(StringUtils.equals(type, RegisterType.mobile.name())) {
            messageService.findMessage(loginName, RegisterType.mobile, handler -> {
                if (handler.succeeded()) {
                    final JsonObject message = handler.result();
                    if (Objects.nonNull(message)
                            && StringUtils.endsWithIgnoreCase(message.getString("code"), params.getString("code"))) {
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
     * @param context 上下文
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
        final JsonObject params = context.getBodyAsJson();
        final String loginName = params.getString("loginName");
        LOGGER.info("邮箱激活账号：{}, 验证码：{}", loginName, params.getString("code"));
        userService.findEmailUser(loginName, handler -> {
            if (handler.failed()) {
                LOGGER.error("查询邮箱激活账户异常【{}】", loginName);
                this.returnWithFailureMessage(context, "账户不存在");
            } else {
                JsonObject user = handler.result();
                if(Objects.isNull(user)){
                    LOGGER.error("邮箱激活未查询到用户【{}】", loginName);
                    this.returnWithFailureMessage(context, "账户不存在");
                    return ;
                }
                ;
                IMessageService messageService = new ServiceProxyBuilder(vertx.getDelegate())
                        .setAddress(IMessageService.MESSAGE_SERVICE_ADDRESS).build(IMessageService.class);
                messageService.findMessage(loginName, RegisterType.email, emailHandler -> {
                    if(emailHandler.failed()){
                        LOGGER.error("调用短信验证码信息错误，", emailHandler.cause());
                        this.returnWithFailureMessage(context, "调用短信验证码信息错误");
                    } else {
                        if(StringUtils.equals(emailHandler.result().getString("code"), params.getString("code"))){
                            this.returnWithSuccessMessage(context, "激活成功");
                            userService.activateEmailUser(user.getLong("user_id"), user.getLong("versions"), activateHandler -> {});
                            messageService.updateMessage(loginName, RegisterType.email, messageHandler -> {});
                            return ;
                        }
                        this.returnWithFailureMessage(context, "激活失败！");
                    }
                });
            }
        });
    }

    /**
     * 修改密码
     * @param context 上下文
     */
    private void changePwdHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
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
            String password = hashStrategy.computeHash(context.getBodyAsJson().getString("pwd"), user.getString("salt"), -1);
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
