package com.ecit.api;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.constants.Constants;
import com.ecit.common.enums.RegisterType;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.enmu.CertifiedType;
import com.ecit.enmu.UserSex;
import com.ecit.handler.*;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by shwang on 2018/2/2.
 */
public class RestUserRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestUserRxVerticle.class);
    private static final String HTTP_USER_SERVICE = "http_user_service_api";
    private ShopHashStrategy hashStrategy;
    private IUserHandler userHandler;
    private ICertifiedHandler certifiedHandler;
    private IAddressHandler addressHandler;

    @Override
    public void start() throws Exception {
        super.start();
        this.userHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IUserHandler.USER_SERVICE_ADDRESS).build(IUserHandler.class);
        this.certifiedHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICertifiedHandler.CERTIFIED_SERVICE_ADDRESS).build(ICertifiedHandler.class);
        this.addressHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IAddressHandler.ADDRESS_SERVICE_ADDRESS).build(IAddressHandler.class);
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post("/register").handler(this::registerHandler);
        router.get("/activate/:loginName/:code").handler(this::activateHandler);

        /**
         * 登录拦截
         */
        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        router.put("/changepwd").handler(this::changePwdHandler);
        router.put("/changeEmail").handler(this::changeEmailHandler);
        router.get("/getUserInfo").handler(this::getUserInfoHandler);
        router.post("/saveUserInfo").handler(this::saveUserInfoHandler);
        router.get("/findUserCertified").handler(this::findUserCertifiedHandler);
        router.post("/insertAddress").handler(this::insertAddressHandler);
        router.put("/updateAddress").handler(this::updateAddressHandler);
        router.put("/updateDefaultAddress/:addressId").handler(this::updateDefaultAddressHandler);
        router.delete("/deleteAddress/:addressId").handler(this::deleteAddressHandler);
        router.get("/findAddress").handler(this::findAddressHandler);
        router.get("/getAddressById/:addressId").handler(this::getAddressByIdHandler);
        router.put("/idcardCertified").handler(this::idcardCertifiedHandler);
        router.get("/getIdcardCertified").handler(this::getIdcardCertifiedHandler);
        router.put("/bindMobile").handler(this::bindMobileHandler);

        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("user.http.address", "localhost");
        int port = config().getInteger("user.http.port", 8080);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            this.hashStrategy = new DefaultHashStrategy(vertx.getDelegate());
            this.publishHttpEndpoint(HTTP_USER_SERVICE, host, port, "user.api.name").subscribe();
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
        IMessageHandler messageService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).build(IMessageHandler.class);
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
        final long userId = IdBuilder.getUniqueId();
        userHandler.register(userId, StringUtils.equals(type, RegisterType.loginName.name()) ? loginName : null,
                StringUtils.equals(type, RegisterType.mobile.name()) ? loginName : null,
                StringUtils.equals(type, RegisterType.email.name()) ? loginName : null,
                hashStrategy.computeHash(password, salt, -1),
                salt,
                registerHandler -> {
                    if (registerHandler.succeeded()) {
                        if (registerHandler.result() >= 1) {
                            certifiedHandler.sendUserCertified(userId, CertifiedType.LOGIN_CERTIFIED.getKey(), "", handler -> {});
                            if (StringUtils.equals(type, RegisterType.mobile.name())) {
                                certifiedHandler.sendUserCertified(userId, CertifiedType.MOBILE_CERTIFIED.getKey(), loginName, handler -> {});
                            } else if (StringUtils.equals(type, RegisterType.email.name())) {
                                certifiedHandler.sendUserCertified(userId, CertifiedType.EMAIL_CERTIFIED.getKey(), loginName, handler -> {});
                            }
                            this.Ok(context, ResultItems.getReturnItemsSuccess("注册成功"));
                        } else {
                            this.Ok(context, ResultItems.getReturnItemsFailure("注册失败"));
                        }
                    } else {
                        this.internalError(context, registerHandler.cause());
                    }
                });
    }

    /**
     * 邮箱激活
     * @param context
     */
    private void activateHandler(RoutingContext context) {
        final JsonObject params = context.getBodyAsJson();
        final String loginName = params.getString("loginName");
        LOGGER.info("邮箱激活账号：{}, 验证码：{}", loginName, params.getString("code"));
        userHandler.findEmailUser(loginName, handler -> {
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
                IMessageHandler messageService = new ServiceProxyBuilder(vertx.getDelegate())
                        .setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).build(IMessageHandler.class);
                messageService.findMessage(loginName, RegisterType.email, emailHandler -> {
                    if(emailHandler.failed()){
                        LOGGER.error("调用短信验证码信息错误，", emailHandler.cause());
                        this.returnWithFailureMessage(context, "调用短信验证码信息错误");
                    } else {
                        if(StringUtils.equals(emailHandler.result().getString("code"), params.getString("code"))){
                            this.returnWithSuccessMessage(context, "激活成功");
                            userHandler.activateEmailUser(user.getLong("user_id"), user.getLong("versions"), activateHandler -> {});
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
    private void changePwdHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        if (!StringUtils.equals(params.getString("pwd"), params.getString("confirm_pwd"))) {
            LOGGER.error("新密码和确认密码不一致！");
            this.returnWithFailureMessage(context, "新密码和确认密码不一致！");
            return ;
        }
        final String token = context.request().getHeader(Constants.TOKEN);
        params.put("strategy", hashStrategy);
        userHandler.changePwdHandler(token, params, handler -> {
           if(handler.failed()){
               this.returnWithFailureMessage(context, "修改密码失败，请重试！");
               return ;
           }
           this.returnWithSuccessMessage(context, "修改密码成功！");
           return ;
        });
    }

    /**
     * 修改密码
     * @param context
     */
    private void changeEmailHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        final String token = context.request().getHeader(Constants.TOKEN);
        userHandler.changeEmailHandler(token, params, handler -> {
            if(handler.failed()){
                LOGGER.error("修改用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "修改邮箱失败!");
                return ;
            }
            this.returnWithSuccessMessage(context, "修改邮箱成功");
            return ;
        });
    }

    /**
     * 获取用户详情信息
     * @param context
     */
    private void getUserInfoHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        userHandler.getUserInfoHandler(token, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "获取用户信息失败!");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询用户详情成功", handler.result());

        });
    }

    /**
     * 保存个人信息
     * @param context
     */
    private void saveUserInfoHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        JsonObject params = context.getBodyAsJson();
        userHandler.saveUserInfoHandler(token, params, handler -> {
            if(handler.failed()){
                LOGGER.error("更新用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "更新用户信息失败!");
                return ;
            }
            this.returnWithSuccessMessage(context, "更新用户详情成功", handler.result());
            return ;
        });
    }

    /**
     * 查询用户认证信息
     * @param context
     */
    private void findUserCertifiedHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        certifiedHandler.findUserCertifiedByUserIdHandler(token, handler -> {
                    if(handler.failed()){
                        LOGGER.error("查询用户认证信息失败！", handler.cause());
                        this.returnWithFailureMessage(context, "查询用户认证信息失败！");
                        return ;
                    }
                    this.returnWithSuccessMessage(context, "查询用户认证信息成功", handler.result());
                    return;
                });
    }

    /**
     *  新增收货地址
     * @param context
     */
    private void insertAddressHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonObject params = context.getBodyAsJson();
        addressHandler.insertAddressHandler(token, params, handler -> {
            if(handler.failed()){
                LOGGER.error("保存收货地址信息失败！", handler.cause());
                this.returnWithFailureMessage(context, "保存收货地址信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "保存收货地址信息成功", handler.result());
            return ;
        });
    }

    /**
     *  更新收货地址
     * @param context
     */
    private void updateAddressHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        final JsonObject params = context.getBodyAsJson();
        addressHandler.updateAddressHandler(token, params, handler -> {
            if(handler.failed()){
                LOGGER.error("修改收货地址信息失败！", handler.cause());
                this.returnWithFailureMessage(context, "修改收货地址信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "修改收货地址信息成功", handler.result());

        });
    }

    /**
     *  收货地址置为默认地址
     * @param context
     */
    private void updateDefaultAddressHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        addressHandler.updateDefaultAddress(token, Long.parseLong(context.pathParam("addressId")),
                handler -> {
                    if(handler.failed()){
                        LOGGER.error("设置收货地址失败！", handler.cause());
                        this.returnWithFailureMessage(context, "设置收货地址失败！");
                        return ;
                    }
                    this.returnWithSuccessMessage(context, "设置收货地址成功", handler.result());
                    return ;
                });
    }

    /**
     *  删除收货地址
     * @param context
     */
    private void deleteAddressHandler(RoutingContext context){
        addressHandler.deleteAddress(Long.parseLong(context.pathParam("addressId")), handler -> {
            if(handler.failed()){
                LOGGER.error("删除收货地址失败！", handler.cause());
                this.returnWithFailureMessage(context, "删除收货地址失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "删除收货地址成功", handler.result());
            return ;
        });
    }

    /**
     *  查询收货地址
     * @param context
     */
    private void findAddressHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        addressHandler.findAddress(token, handler -> {
            if(handler.failed()){
                LOGGER.error("查询收货地址失败！", handler.cause());
                this.returnWithFailureMessage(context, "查询收货地址失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询收货地址成功", handler.result());
            return ;
        });
    }

    /**
     *
     * @param context
     */
    private void getAddressByIdHandler(RoutingContext context){
        addressHandler.getAddressById(Long.parseLong(context.pathParam("addressId")), handler -> {
            if(handler.failed()){
                LOGGER.error("查询收货地址失败！", handler.cause());
                this.returnWithFailureMessage(context, "查询收货地址失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询收货地址成功", handler.result());

        });
    }

    /**
     * 实名认证
     * @param context
     */
    private void idcardCertifiedHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        userHandler.updateIdcardHandler(token, context.getBodyAsJson(), handler -> {
            if(handler.failed()){
                LOGGER.error("更新实名认证失败！", handler.cause());
                this.returnWithFailureMessage(context, "实名认证失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "实名认证成功", handler.result());
            return ;
        });
    }

    /**
     * 获取身份认证信息
     * @param context
     */
    private void getIdcardCertifiedHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        userHandler.getIdcardInfoHandler(token, handler -> {
            if(handler.failed()){
                LOGGER.error("查询实名认证信息失败！", handler.cause());
                this.returnWithFailureMessage(context, "查询认证信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询实名认证信息成功", handler.result());
            return ;
        });
    }

    /**
     * 绑定手机号码
     * @param context
     */
    private void bindMobileHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        userHandler.bindMobileHandler(token, context.getBodyAsJson(), handler -> {
            if(handler.failed()){
                LOGGER.error("绑定手机号码失败！", handler.cause());
                this.returnWithFailureMessage(context, "绑定手机号码失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "绑定手机号码成功", handler.result());
        });
    }


}
