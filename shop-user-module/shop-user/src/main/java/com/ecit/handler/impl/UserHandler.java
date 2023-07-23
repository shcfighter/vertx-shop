package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.enums.IsDeleted;
import com.ecit.common.enums.RegisterType;
import com.ecit.common.utils.FormatUtils;
import com.ecit.common.utils.JsonUtils;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.constants.UserSql;
import com.ecit.enmu.CertifiedType;
import com.ecit.enmu.UserSex;
import com.ecit.enmu.UserStatus;
import com.ecit.handler.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Objects;

/**
 * Created by shwang on 2018/2/2.
 */
public class UserHandler extends JdbcRxRepositoryWrapper implements IUserHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserHandler.class);
    final Vertx vertx;
    private IUserHandler userHandler;
    private ICertifiedHandler certifiedHandler;
    private IAddressHandler addressHandler;
    private IMessageHandler messageHandler;
    public UserHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.userHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IUserHandler.USER_SERVICE_ADDRESS).build(IUserHandler.class);
        this.certifiedHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICertifiedHandler.CERTIFIED_SERVICE_ADDRESS).build(ICertifiedHandler.class);
        this.addressHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IAddressHandler.ADDRESS_SERVICE_ADDRESS).build(IAddressHandler.class);
        this.messageHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).build(IMessageHandler.class);
    }

    @Override
    public IUserHandler register(long userId, String loginName, String mobile, String email, String pwd, String salt,
                                 Handler<AsyncResult<Integer>> resultHandler) {
        Promise<Integer> accountPromise = Promise.promise();
        IAccountHandler accountService = new ServiceProxyBuilder(vertx.getDelegate())
                .setAddress(IAccountHandler.ACCOUNT_SERVICE_ADDRESS).build(IAccountHandler.class);
        accountService.insertAccount(userId, accountPromise);
        accountPromise.future().compose(account -> {
            Promise<Integer> userPromise = Promise.promise();
            this.execute(Tuple.tuple().addLong(userId).addString(StringUtils.isEmpty(loginName) ? "member_" + userId : loginName).addString(pwd)
                            .addInteger(Objects.isNull(email) ? UserStatus.ACTIVATION.getStatus() : UserStatus.INACTIVATED.getStatus())
                            .addInteger(IsDeleted.NO.getValue())
                            .addString(Objects.isNull(mobile) ? "" : mobile).addString(Objects.isNull(email) ? "" : email).addString(salt),
                    UserSql.REGISTER_SQL).subscribe(userPromise::complete, userPromise::fail);
            return userPromise.future();
        }).onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler login(String loginName, String pwd, Handler<AsyncResult<JsonObject>> resultHandler) {
        Promise<JsonObject> promise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addInteger(UserStatus.ACTIVATION.getStatus()).addInteger(IsDeleted.NO.getValue())
                        .addString(loginName).addString(loginName).addString(loginName),
                UserSql.LOGIN_SQL).subscribe(promise::complete, promise::fail);
        promise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler changePwd(long userId, String pwd, long versions, Handler<AsyncResult<Integer>> resultHandler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addString(pwd).addLong(versions + 1).addLong(userId).addLong(versions), UserSql.CHANGE_PWD_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler getMemberById(long userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Promise<JsonObject> promise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addLong(userId).addInteger(IsDeleted.NO.getValue()), UserSql.GET_USER_BY_ID_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler activateEmailUser(long userId, long versions, Handler<AsyncResult<Integer>> resultHandler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addLong(userId).addLong(versions), UserSql.ACTIVATE_EMAIL_USER_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler findEmailUser(String loginName, Handler<AsyncResult<JsonObject>> resultHandler) {
        Promise<JsonObject> userPromise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addString(loginName), UserSql.ACTIVATE_EMAIL_USER_SELECT_SQL)
                .subscribe(userPromise::complete, userPromise::fail);
        userPromise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler getUserInfoHandler(String token, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<JsonObject> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            long userId = session.getLong("userId");
            Promise<JsonObject> userPromise = Promise.promise();
            this.retrieveOne(Tuple.tuple().addLong(userId), UserSql.GET_USER_INFO_SQL)
                    .subscribe(userPromise::complete, userPromise::fail);
            return userPromise.future();
        });

        resultFuture.onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler saveUserInfoHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<JsonObject> userPromise = Promise.promise();
            this.retrieveOne(Tuple.tuple().addLong(userId), UserSql.GET_USER_INFO_SQL)
                    .subscribe(userPromise::complete, userPromise::fail);
            return userPromise.future().compose(user -> {
                Promise<Integer> updatePromise = Promise.promise();
                pgPool.rxGetConnection()
                    .flatMap(conn ->
                        conn.preparedQuery(UserSql.UPDATE_USER_SQL)
                                .rxExecute(Tuple.tuple().addString(params.getString("login_name")).addString(params.getString("user_name"))
                                        .addString(params.getString("mobile")).addString(params.getString("email"))
                                        .addLong(userId).addLong(user.getLong("versions")))
                            .flatMap(updateResult -> {
                                if (Objects.isNull(user.getLong("info_versions"))) {
                                    return conn.preparedQuery(UserSql.INSERT_USER_INFO_SQL)
                                            .rxExecute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(userId).addString(params.getString("user_name"))
                                                    .addInteger(Objects.nonNull(params.getInteger("sex")) ? params.getInteger("sex") : UserSex.CONFIDENTIALITY.getKey())
                                                    .addString(DateFormatUtils.format(new Date(Objects.isNull(params.getLong("birthday")) ? 0 : params.getLong("birthday")), FormatUtils.DATE_FORMAT))
                                                    .addString(params.getString("photo_url")));
                                } else {
                                    return conn.preparedQuery(UserSql.UPDATE_USER_INFO_SQL).rxExecute(Tuple.tuple().addString(params.getString("user_name")).addInteger(Objects.nonNull(params.getInteger("sex")) ? params.getInteger("sex") : UserSex.CONFIDENTIALITY.getKey())
                                            .addString(DateFormatUtils.format(new Date(Objects.isNull(params.getLong("birthday")) ? 0 : params.getLong("birthday")), FormatUtils.DATE_FORMAT))
                                            .addString(params.getString("photo_url")).addLong(userId).addLong(user.getLong("info_versions")));
                                }
                            }).doAfterTerminate(conn::close)
                    ).subscribe(update -> updatePromise.complete(update.rowCount()), updatePromise::fail);
                return updatePromise.future();
            });
        });
        resultFuture.onComplete(resultHandler);
        return this;
    }

    @Override
    public IUserHandler updateEmail(long userId, String email, long versions, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addString(email).addLong(userId).addLong(versions), UserSql.UPDATE_USER_EMAIL_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public IUserHandler updateIdcardHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            long userId = session.getLong("userId");
            Promise<JsonObject> promise = Promise.promise();
            this.retrieveOne(Tuple.tuple().addLong(userId), UserSql.SELECT_USER_INFO_BY_USERID_SQL)
                    .subscribe(promise::complete, promise::fail);
            return promise.future().compose(userInfo -> {
                if (Objects.isNull(userInfo) || userInfo.size() == 0) {
                    Promise<Integer> insertPromise = Promise.promise();
                    this.execute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(userId).addString(params.getString("real_name"))
                                    .addString(params.getString("id_card")).addString(params.getString("id_card_positive_url"))
                                            .addString(params.getString("id_card_negative_url"))
                            , UserSql.INSERT_USER_INFO_IDCARD_SQL)
                            .subscribe(insertPromise::complete, insertPromise::fail);
                    return insertPromise.future();
                } else {
                    Promise<Integer> updatePromise = Promise.promise();
                    this.execute(Tuple.tuple().addString(params.getString("real_name")).addString(params.getString("id_card"))
                                    .addString(params.getString("id_card_positive_url")).addString(params.getString("id_card_negative_url"))
                                    .addLong(userId).addLong(userInfo.getLong("versions")),
                            UserSql.UPDATE_USER_IDCARD_SQL)
                            .subscribe(updatePromise::complete, updatePromise::fail);
                    return updatePromise.future();
                }
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IUserHandler getIdcardInfoHandler(String token, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<JsonObject> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            Promise<JsonObject> promise = Promise.promise();
            this.retrieveOne(Tuple.tuple().addLong(session.getLong("userId")), UserSql.GET_USER_IDCARD_INFO_SQL)
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IUserHandler getBindMobile(long userId, Handler<AsyncResult<JsonObject>> handler) {
        return null;
    }

    @Override
    public IUserHandler bindMobileHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            long userId = session.getLong("userId");
            Promise<JsonObject> userPromise = Promise.promise();
            this.getMemberById(userId, userPromise);
            return userPromise.future().compose(user -> {
                if(JsonUtils.isNull(user)){
                    LOGGER.error("【绑定手机号】查询用户【{}】信息不存在！", userId);
                    return Future.failedFuture("用户信息不存在！");
                }
                Promise<JsonObject> messagePromise = Promise.promise();
                messageHandler.findMessage(params.getString("mobile"), RegisterType.mobile, messagePromise);
                return messagePromise.future();
            }).compose(message -> {
                if(JsonUtils.isNull(message)){
                    LOGGER.error("【绑定手机号】{}查询验证码信息不存在！", params.getString("mobile"));
                    return Future.failedFuture("查询验证码信息不存在！");
                }
                if(!StringUtils.equals(params.getString("code"), message.getString("code"))){
                    LOGGER.error("【绑定手机号】{}, 验证码【{}】不正确！", params.getString("mobile"), message.getString("code"));
                    return Future.failedFuture("验证码错误！");
                }
                return Future.succeededFuture(params.getString("code"));
            }).compose(messageCode -> {
                JsonObject user = userPromise.future().result();
                Promise<Integer> promise = Promise.promise();
                this.execute(Tuple.tuple().addString(params.getString("mobile")).addLong(userId).addLong(user.getLong("versions")), UserSql.BIND_MOBILE_SQL)
                        .subscribe(promise::complete, promise::fail);
                certifiedHandler.sendUserCertified(userId, CertifiedType.MOBILE_CERTIFIED.getKey(), params.getString("mobile"), certifiedHandler -> {});
                messageHandler.updateMessage(params.getString("mobile"), RegisterType.mobile, messageHandler -> {});
                return promise.future();
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IUserHandler changePwdHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        LOGGER.info("params:{}", params.encodePrettily());
        Future<JsonObject> sessionFuture = this.getSession(token);
        final ShopHashStrategy hashStrategy = (ShopHashStrategy) params.getValue("strategy");
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            LOGGER.info("session:{}", session.encodePrettily());
            if(JsonUtils.isNull(session)){
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<JsonObject> userPromise = Promise.promise();
            userHandler.getMemberById(userId, userPromise);
            return userPromise.future().compose(user -> {
                LOGGER.info("user:{}", user.encodePrettily());
                final String originalPwd = hashStrategy.computeHash(params.getString("original_pwd"), user.getString("salt"), -1);
                if(!StringUtils.equals(originalPwd, user.getString("password"))){
                    LOGGER.error("原密码错误！");
                    return Future.failedFuture("原密码错误！");
                }
                /**
                 * 密码加密
                 */
                final String password = hashStrategy.computeHash(params.getString("pwd"), user.getString("salt"), -1);
                Promise<Integer> changePwdPromise = Promise.promise();
                userHandler.changePwd(userId, password, user.getLong("versions"), changePwdPromise);
                return changePwdPromise.future().compose(change -> {
                    LOGGER.info("change:{}", change);
                    if(change > 0){
                        certifiedHandler.sendUserCertified(userId, CertifiedType.LOGIN_CERTIFIED.getKey(), "", handler3 -> {});
                    }
                    Promise<Integer> promise = Promise.promise();
                    promise.complete(change);
                    return promise.future();
                });
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IUserHandler changeEmailHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if(JsonUtils.isNull(session)){
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<JsonObject> userPromise = Promise.promise();
            userHandler.getMemberById(userId, userPromise);
            return userPromise.future().compose(user -> {
                if(JsonUtils.isNull(user)){
                    LOGGER.info("无法获取user信息");
                    return Future.failedFuture("can not get user");
                }
                final String email = params.getString("email");
                final String code = params.getString("code");
                Promise<JsonObject> messagePromise = Promise.promise();
                messageHandler.findMessage(email, RegisterType.email, messagePromise);
                return messagePromise.future().compose(message -> {
                    if(JsonUtils.isNull(message)){
                        LOGGER.info("无法获取验证码信息");
                        return Future.failedFuture("can not get check message");
                    }
                    if (Objects.nonNull(message)
                            && StringUtils.endsWithIgnoreCase(message.getString("code"), code)) {
                        Promise<Integer> updatePromise = Promise.promise();
                        userHandler.updateEmail(userId, email, user.getLong("versions"), updatePromise);
                        return updatePromise.future().compose(update -> {
                            certifiedHandler.sendUserCertified(userId, CertifiedType.EMAIL_CERTIFIED.getKey(), email, certifiedHandler -> {
                            });
                            messageHandler.updateMessage(email, RegisterType.email, deleteHandler -> {
                            });
                            return updatePromise.future();
                        });
                    } else {
                        LOGGER.info("验证码错误");
                        return Future.failedFuture("check code error");
                    }
                });
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }


}
