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
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by shwang on 2018/2/2.
 */
public class UserHandler extends JdbcRxRepositoryWrapper implements IUserHandler {

    private static final Logger LOGGER = LogManager.getLogger(UserHandler.class);
    final Vertx vertx;
    private IUserHandler userHandler;
    private ICertifiedHandler certifiedHandler;
    private IAddressHandler addressHandler;
    private IMessageHandler messageService;
    private ICertifiedHandler certifiedService;
    public UserHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.certifiedService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICertifiedHandler.CERTIFIED_SERVICE_ADDRESS).build(ICertifiedHandler.class);
        this.userHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IUserHandler.USER_SERVICE_ADDRESS).build(IUserHandler.class);
        this.certifiedHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICertifiedHandler.CERTIFIED_SERVICE_ADDRESS).build(ICertifiedHandler.class);
        this.addressHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IAddressHandler.ADDRESS_SERVICE_ADDRESS).build(IAddressHandler.class);
        this.messageService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).build(IMessageHandler.class);
    }

    @Override
    public IUserHandler register(long userId, String loginName, String mobile, String email, String pwd, String salt,
                                 Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> accountFuture = Future.future();
        IAccountHandler accountService = new ServiceProxyBuilder(vertx.getDelegate())
                .setAddress(IAccountHandler.ACCOUNT_SERVICE_ADDRESS).build(IAccountHandler.class);
        accountService.insertAccount(userId, accountFuture);
        Future<Integer> future = accountFuture.compose(account -> {
            Future<Integer> userFuture = Future.future();
            this.execute(new JsonArray().add(userId).add(StringUtils.isEmpty(loginName) ? "member_" + userId : loginName).add(pwd)
                            .add(Objects.isNull(email) ? UserStatus.ACTIVATION.getStatus() : UserStatus.INACTIVATED.getStatus()).add(IsDeleted.NO.getValue())
                            .add(Objects.isNull(mobile) ? "" : mobile).add(Objects.isNull(email) ? "" : email).add(salt),
                    UserSql.REGISTER_SQL).subscribe(userFuture::complete, userFuture::fail);
            return userFuture;
        }).setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler login(String loginName, String pwd, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(UserStatus.ACTIVATION.getStatus()).add(IsDeleted.NO.getValue())
                        .add(loginName).add(loginName).add(loginName),
                UserSql.LOGIN_SQL).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler changePwd(long userId, String pwd, long versions, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(pwd).add(versions + 1).add(userId).add(versions), UserSql.CHANGE_PWD_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler getMemberById(long userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(userId).add(IsDeleted.NO.getValue()), UserSql.GET_USER_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler activateEmailUser(long userId, long versions, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(userId).add(versions), UserSql.ACTIVATE_EMAIL_USER_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler findEmailUser(String loginName, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(loginName), UserSql.ACTIVATE_EMAIL_USER_SELECT_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
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
            Future<JsonObject> userFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId), UserSql.GET_USER_INFO_SQL)
                    .subscribe(userFuture::complete, userFuture::fail);
            return userFuture;
        });

        resultFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler saveUserInfoHandler(String token, JsonObject params, Handler<AsyncResult<UpdateResult>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<UpdateResult> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Future<JsonObject> userFuture = Future.future();
            this.retrieveOne(new JsonArray().add(userId), UserSql.GET_USER_INFO_SQL)
                    .subscribe(userFuture::complete, userFuture::fail);
            return userFuture.compose(user -> {
                Future updateFuture = Future.future();
                postgreSQLClient.rxGetConnection()
                    .flatMap(conn ->
                        conn.rxSetAutoCommit(false).toSingleDefault(false)
                            .flatMap(autoCommit -> conn.rxUpdateWithParams(UserSql.UPDATE_USER_SQL,
                                    new JsonArray().add(params.getString("login_name")).add(params.getString("user_name")).add(params.getString("mobile"))
                                            .add(params.getString("email")).add(userId).add(user.getLong("versions"))))
                            .flatMap(updateResult -> {
                                if (Objects.isNull(user.getLong("info_versions"))) {
                                    return conn.rxUpdateWithParams(UserSql.INSERT_USER_INFO_SQL,
                                            new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(params.getString("user_name")).add(Objects.nonNull(params.getInteger("sex")) ? params.getInteger("sex") : UserSex.CONFIDENTIALITY.getKey())
                                                    .add(DateFormatUtils.format(new Date(Objects.isNull(params.getLong("birthday")) ? 0 : params.getLong("birthday")), FormatUtils.DATE_FORMAT)).add(params.getString("photo_url")));
                                } else {
                                    return conn.rxUpdateWithParams(UserSql.UPDATE_USER_INFO_SQL,
                                        new JsonArray().add(params.getString("user_name")).add(Objects.nonNull(params.getInteger("sex")) ? params.getInteger("sex") : UserSex.CONFIDENTIALITY.getKey())
                                                .add(DateFormatUtils.format(new Date(Objects.isNull(params.getLong("birthday")) ? 0 : params.getLong("birthday")), FormatUtils.DATE_FORMAT)).add(params.getString("photo_url"))
                                                .add(userId).add(user.getLong("info_versions")));
                                }
                            })
                            // Rollback if any failed with exception propagation
                            .onErrorResumeNext(ex -> conn.rxRollback()
                                    .toSingleDefault(true)
                                    .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                                    .flatMap(ignore -> Single.error(ex))
                            )
                            // close the connection regardless succeeded or failed
                            .doAfterTerminate(conn::close)
                    ).subscribe(updateFuture::complete, updateFuture::fail);
                return updateFuture;
            });
        });
        resultFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserHandler updateEmail(long userId, String email, long versions, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(email).add(userId).add(versions), UserSql.UPDATE_USER_EMAIL_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
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
            Future<JsonObject> future = Future.future();
            this.retrieveOne(new JsonArray().add(userId), UserSql.SELECT_USER_INFO_BY_USERID_SQL)
                    .subscribe(future::complete, future::fail);
            return future.compose(userInfo -> {
                if (Objects.isNull(userInfo) || userInfo.size() == 0) {
                    Future<Integer> insertFuture = Future.future();
                    this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(params.getString("real_name"))
                                    .add(params.getString("id_card")).add(params.getString("id_card_positive_url")).add(params.getString("id_card_negative_url"))
                            , UserSql.INSERT_USER_INFO_IDCARD_SQL)
                            .subscribe(insertFuture::complete, insertFuture::fail);
                    return insertFuture;
                } else {
                    Future<Integer> updateFuture = Future.future();
                    this.execute(new JsonArray().add(params.getString("real_name")).add(params.getString("id_card"))
                                    .add(params.getString("id_card_positive_url")).add(params.getString("id_card_negative_url"))
                                    .add(userId).add(userInfo.getLong("versions")),
                            UserSql.UPDATE_USER_IDCARD_SQL)
                            .subscribe(updateFuture::complete, updateFuture::fail);
                    return updateFuture;
                }
            });
        });
        resultFuture.setHandler(handler);
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
            Future<JsonObject> future = Future.future();
            this.retrieveOne(new JsonArray().add(session.getLong("userId")), UserSql.GET_USER_IDCARD_INFO_SQL)
                    .subscribe(future::complete, future::fail);
            return future;
        });
        resultFuture.setHandler(handler);
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
            Future<JsonObject> userFuture = Future.future();
            this.getMemberById(userId, userFuture);
            return userFuture.compose(user -> {
                if(JsonUtils.isNull(user)){
                    LOGGER.error("【绑定手机号】查询用户【{}】信息不存在！", userId);
                    return Future.failedFuture("用户信息不存在！");
                }
                Future<JsonObject> messageFuture = Future.future();
                messageService.findMessage(params.getString("mobile"), RegisterType.mobile, messageFuture);
                return messageFuture;
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
                JsonObject user = userFuture.result();
                Future<Integer> future = Future.future();
                this.execute(new JsonArray().add(params.getString("mobile")).add(userId).add(user.getLong("versions")), UserSql.BIND_MOBILE_SQL)
                        .subscribe(future::complete, future::fail);
                certifiedService.sendUserCertified(userId, CertifiedType.MOBILE_CERTIFIED.getKey(), params.getString("mobile"), certifiedHandler -> {});
                messageService.updateMessage(params.getString("mobile"), RegisterType.mobile, messageHandler -> {});
                return future;
            });
        });
        resultFuture.setHandler(handler);
        return this;
    }

    @Override
    public IUserHandler changePwdHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        final ShopHashStrategy hashStrategy = (ShopHashStrategy) params.getValue("strategy");
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if(JsonUtils.isNull(session)){
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Future<JsonObject> userFuture = Future.future();
            userHandler.getMemberById(userId, userFuture);
            return userFuture.compose(user -> {
                final String originalPwd = hashStrategy.computeHash(params.getString("original_pwd"), user.getString("salt"), -1);
                if(!StringUtils.equals(originalPwd, user.getString("password"))){
                    LOGGER.error("原密码错误！");
                    return Future.failedFuture("原密码错误！");
                }
                /**
                 * 密码加密
                 */
                final String password = hashStrategy.computeHash(params.getString("pwd"), user.getString("salt"), -1);
                Future<Integer> changePwdFuture = Future.future();
                userHandler.changePwd(userId, password, user.getLong("versions"), changePwdFuture);
                return changePwdFuture.compose(change -> {
                    if(change > 0){
                        certifiedHandler.sendUserCertified(userId, CertifiedType.LOGIN_CERTIFIED.getKey(), "", handler3 -> {});
                    }
                    return changePwdFuture;
                });
            });
        });
        resultFuture.setHandler(handler);
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
            Future<JsonObject> userFuture = Future.future();
            userHandler.getMemberById(userId, userFuture);
            return userFuture.compose(user -> {
                if(JsonUtils.isNull(user)){
                    LOGGER.info("无法获取user信息");
                    return Future.failedFuture("can not get user");
                }
                final String email = params.getString("email");
                final String code = params.getString("code");
                Future<JsonObject> messageFuture = Future.future();
                messageService.findMessage(email, RegisterType.email, messageFuture);
                return messageFuture.compose(message -> {
                    if(JsonUtils.isNull(message)){
                        LOGGER.info("无法获取验证码信息");
                        return Future.failedFuture("can not get check message");
                    }
                    if (Objects.nonNull(message)
                            && StringUtils.endsWithIgnoreCase(message.getString("code"), code)) {
                        Future<Integer> updateFuture = Future.future();
                        userHandler.updateEmail(userId, email, user.getLong("versions"), updateFuture);
                        return updateFuture.compose(update -> {
                            certifiedHandler.sendUserCertified(userId, CertifiedType.EMAIL_CERTIFIED.getKey(), email, certifiedHandler -> {
                            });
                            messageService.updateMessage(email, RegisterType.email, deleteHandler -> {
                            });
                            return updateFuture;
                        });
                    } else {
                        LOGGER.info("验证码错误");
                        return Future.failedFuture("check code error");
                    }
                });
            });
        });
        resultFuture.setHandler(handler);
        return this;
    }


}
