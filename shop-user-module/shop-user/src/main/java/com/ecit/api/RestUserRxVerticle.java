package com.ecit.api;

import com.ecit.common.enmu.RegisterType;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.enmu.CertifiedType;
import com.ecit.enmu.UserSex;
import com.ecit.service.*;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestUserRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestUserRxVerticle.class);
    private static final String HTTP_USER_SERVICE = "http_user_service_api";
    private ShopHashStrategy hashStrategy;
    private final IUserService userService;
    private final ICertifiedService certifiedService;
    private final IAddressService addressService;

    public RestUserRxVerticle(IUserService userService, ICertifiedService certifiedService, IAddressService addressService) {
        this.userService = userService;
        this.certifiedService = certifiedService;
        this.addressService = addressService;
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
        router.put("/changeEmail").handler(context -> this.requireLogin(context, this::changeEmailHandler));
        router.get("/getUserInfo").handler(context -> this.requireLogin(context, this::getUserInfoHandler));
        router.post("/saveUserInfo").handler(context -> this.requireLogin(context, this::saveUserInfoHandler));
        router.get("/findUserCertified").handler(context -> this.requireLogin(context, this::findUserCertifiedHandler));
        router.post("/insertAddress").handler(context -> this.requireLogin(context, this::insertAddressHandler));
        router.put("/updateAddress").handler(context -> this.requireLogin(context, this::updateAddressHandler));
        router.put("/updateDefaultAddress/:addressId").handler(context -> this.requireLogin(context, this::updateDefaultAddressHandler));
        router.delete("/deleteAddress/:addressId").handler(context -> this.requireLogin(context, this::deleteAddressHandler));
        router.get("/findAddress").handler(context -> this.requireLogin(context, this::findAddressHandler));
        router.get("/getAddressById/:addressId").handler(context -> this.requireLogin(context, this::getAddressByIdHandler));
        router.put("/idcardCertified").handler(context -> this.requireLogin(context, this::idcardCertifiedHandler));
        router.get("/getIdcardCertified").handler(context -> this.requireLogin(context, this::getIdcardCertifiedHandler));

        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("user.http.address", "localhost");
        int port = config().getInteger("user.http.port", 8080);

        // create HTTP server and publish REST service
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
        final long userId = IdBuilder.getUniqueId();
        userService.register(userId, StringUtils.equals(type, RegisterType.loginName.name()) ? loginName : null,
                StringUtils.equals(type, RegisterType.mobile.name()) ? loginName : null,
                StringUtils.equals(type, RegisterType.email.name()) ? loginName : null,
                hashStrategy.computeHash(password, salt, -1),
                salt,
                registerHandler -> {
                    if (registerHandler.succeeded()) {
                        if (registerHandler.result() >= 1) {
                            certifiedService.sendUserCertified(userId, CertifiedType.LOGIN_CERTIFIED.getKey(), "", handler -> {});
                            if (StringUtils.equals(type, RegisterType.mobile.name())) {
                                certifiedService.sendUserCertified(userId, CertifiedType.MOBILE_CERTIFIED.getKey(), loginName, handler -> {});
                            } else if (StringUtils.equals(type, RegisterType.email.name())) {
                                certifiedService.sendUserCertified(userId, CertifiedType.EMAIL_CERTIFIED.getKey(), loginName, handler -> {});
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
        JsonObject params = context.getBodyAsJson();
        if (!StringUtils.equals(params.getString("pwd"), params.getString("confirm_pwd"))) {
            LOGGER.error("新密码和确认密码不一致！");
            this.returnWithFailureMessage(context, "新密码和确认密码不一致！");
            return ;
        }
        userService.getMemberById(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "修改密码失败!");
                return ;
            }
            JsonObject user = handler.result();
            final String originalPwd = hashStrategy.computeHash(params.getString("original_pwd"), user.getString("salt"), -1);
            if(!StringUtils.equals(originalPwd, user.getString("password"))){
                LOGGER.error("原密码错误！");
                this.returnWithFailureMessage(context, "原密码错误！");
                return ;
            }

            /**
             * 密码加密
             */
            final String password = hashStrategy.computeHash(params.getString("pwd"), user.getString("salt"), -1);
            userService.changePwd(userId, password,
                    user.getLong("versions"), handler2 -> {
                        if(handler2.failed()){
                            LOGGER.error("修改密码失败", handler2.cause());
                            this.returnWithFailureMessage(context, "修改密码失败!");
                            return ;
                        }
                        certifiedService.sendUserCertified(userId, CertifiedType.LOGIN_CERTIFIED.getKey(), "", handler3 -> {});
                        this.returnWithSuccessMessage(context, "修改密码成功");
                    });
        });
    }

    /**
     * 修改密码
     * @param context
     * @param principal
     */
    private void changeEmailHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        if(Objects.isNull(userId) ){
            LOGGER.error("登录id【{}】不存在", userId);
            this.returnWithFailureMessage(context, "登录id【" + userId + "】不存在");
            return ;
        }
        JsonObject params = context.getBodyAsJson();
        userService.getMemberById(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "修改邮箱失败!");
                return ;
            }
            JsonObject user = handler.result();
            final String email = params.getString("email");
            final String code = params.getString("code");

            IMessageService messageService = new ServiceProxyBuilder(vertx.getDelegate())
                    .setAddress(IMessageService.MESSAGE_SERVICE_ADDRESS).build(IMessageService.class);
            messageService.findMessage(email, RegisterType.email, handler2 -> {
                if (handler2.succeeded()) {
                    final JsonObject message = handler2.result();
                    if (Objects.nonNull(message)
                            && StringUtils.endsWithIgnoreCase(message.getString("code"), code)) {
                        userService.updateEmail(userId, email,
                                user.getLong("versions"), handler3 -> {
                                    if(handler2.failed()){
                                        LOGGER.error("修改邮箱失败", handler3.cause());
                                        this.returnWithFailureMessage(context, "修改邮箱失败!");
                                        return ;
                                    }
                                    certifiedService.sendUserCertified(userId, CertifiedType.EMAIL_CERTIFIED.getKey(), email, handler4 -> {});
                                    this.returnWithSuccessMessage(context, "修改邮箱成功");
                                });
                        messageService.updateMessage(email, RegisterType.email, deleteHandler -> {
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
                    LOGGER.info("查询验证码失败", handler2.cause());
                    this.returnWithFailureMessage(context, "验证码错误");
                    return ;
                }
            });
        });
    }

    /**
     * 获取用户详情信息
     * @param context
     * @param principal
     */
    private void getUserInfoHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        userService.getUserInfo(userId, handler -> {
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
     * @param principal
     */
    private void saveUserInfoHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        JsonObject params = context.getBodyAsJson();
        userService.saveUserInfo(userId, params.getString("login_name"), params.getString("user_name"),params.getString("mobile"),
                params.getString("email"),
                Objects.nonNull(params.getInteger("sex")) ? params.getInteger("sex") : UserSex.CONFIDENTIALITY.getKey(),
                Objects.isNull(params.getLong("birthday")) ? 0 : params.getLong("birthday"),
                params.getString("photo_url"),
                handler -> {
            if(handler.failed()){
                LOGGER.error("更新用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "更新用户信息失败!");
                return ;
            }
            this.returnWithSuccessMessage(context, "更新用户详情成功", handler.result());

        });
    }

    /**
     * 查询用户认证信息
     * @param context
     * @param principal
     */
    private void findUserCertifiedHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        certifiedService.findUserCertifiedByUserId(userId, handler -> {
                    if(handler.failed()){
                        LOGGER.error("查询用户认证信息失败！", handler.cause());
                        this.returnWithFailureMessage(context, "查询用户认证信息失败！");
                        return ;
                    }
                    this.returnWithSuccessMessage(context, "查询用户认证信息成功", handler.result());

                });
    }

    /**
     *  新增收货地址
     * @param context
     * @param principal
     */
    private void insertAddressHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        final JsonObject params = context.getBodyAsJson();
        addressService.insertAddress(userId, params.getString("receiver"), params.getString("mobile"),
                params.getString("province_code"), params.getString("city_code"), params.getString("county_code"),
                params.getString("address"), params.getString("address_details"),
                handler -> {
                    if(handler.failed()){
                        LOGGER.error("保存收货地址信息失败！", handler.cause());
                        this.returnWithFailureMessage(context, "保存收货地址信息失败！");
                        return ;
                    }
                    this.returnWithSuccessMessage(context, "保存收货地址信息成功", handler.result());

                });
    }

    /**
     *  更新收货地址
     * @param context
     * @param principal
     */
    private void updateAddressHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        final JsonObject params = context.getBodyAsJson();
        addressService.updateAddress(params.getLong("address_id"), params.getString("receiver"), params.getString("mobile"),
                params.getString("province_code"), params.getString("city_code"), params.getString("county_code"),
                params.getString("address"), params.getString("address_details"),
                handler -> {
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
     * @param principal
     */
    private void updateDefaultAddressHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        addressService.updateDefaultAddress(userId, Long.parseLong(context.pathParam("addressId")),
                handler -> {
                    if(handler.failed()){
                        LOGGER.error("设置收货地址失败！", handler.cause());
                        this.returnWithFailureMessage(context, "设置收货地址失败！");
                        return ;
                    }
                    this.returnWithSuccessMessage(context, "设置收货地址成功", handler.result());

                });
    }

    /**
     *  删除收货地址
     * @param context
     * @param principal
     */
    private void deleteAddressHandler(RoutingContext context, JsonObject principal){
        addressService.deleteAddress(Long.parseLong(context.pathParam("addressId")), handler -> {
                    if(handler.failed()){
                        LOGGER.error("删除收货地址失败！", handler.cause());
                        this.returnWithFailureMessage(context, "删除收货地址失败！");
                        return ;
                    }
                    this.returnWithSuccessMessage(context, "删除收货地址成功", handler.result());

                });
    }

    /**
     *  查询收货地址
     * @param context
     * @param principal
     */
    private void findAddressHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        addressService.findAddress(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("查询收货地址失败！", handler.cause());
                this.returnWithFailureMessage(context, "查询收货地址失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询收货地址成功", handler.result());

        });
    }

    /**
     *
     * @param context
     * @param principal
     */
    private void getAddressByIdHandler(RoutingContext context, JsonObject principal){
        addressService.getAddressById(Long.parseLong(context.pathParam("addressId")), handler -> {
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
     * @param principal
     */
    private void idcardCertifiedHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        JsonObject params = context.getBodyAsJson();
        userService.updateIdcard(userId, params.getString("real_name"), params.getString("id_card"),
                params.getString("id_card_positive_url"), params.getString("id_card_negative_url"),
                handler -> {
            if(handler.failed()){
                LOGGER.error("更新实名认证失败！", handler.cause());
                this.returnWithFailureMessage(context, "实名认证失败！");
                return ;
            }

            this.returnWithSuccessMessage(context, "实名认证成功", handler.result());
        });
    }

    private void getIdcardCertifiedHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        userService.getIdcardInfo(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("查询实名认证信息失败！", handler.cause());
                this.returnWithFailureMessage(context, "查询认证信息失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询实名认证信息成功", handler.result());
        });
    }
}
