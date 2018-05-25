package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.enmu.IsDeleted;
import com.ecit.common.enmu.RegisterType;
import com.ecit.common.utils.FormatUtils;
import com.ecit.common.utils.JsonUtils;
import com.ecit.constants.UserSql;
import com.ecit.enmu.CertifiedType;
import com.ecit.enmu.UserStatus;
import com.ecit.service.ICertifiedService;
import com.ecit.service.IMessageService;
import com.ecit.service.IUserService;
import com.ecit.service.IdBuilder;
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
import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class UserServiceImpl extends JdbcRxRepositoryWrapper implements IUserService{

    private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class);
    final Vertx vertx;
    public UserServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
    }

    @Override
    public IUserService register(long userId, String loginName, String mobile, String email, String pwd, String salt,
                                 Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(userId).add(StringUtils.isEmpty(loginName) ? "member_" + userId : loginName).add(pwd)
                        .add(Objects.isNull(email) ? UserStatus.ACTIVATION.getStatus() : UserStatus.INACTIVATED.getStatus()).add(IsDeleted.NO.getValue())
                        .add(Objects.isNull(mobile) ? "" : mobile).add(Objects.isNull(email) ? "" : email).add(salt),
                UserSql.REGISTER_SQL).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService login(String loginName, String pwd, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(UserStatus.ACTIVATION.getStatus()).add(IsDeleted.NO.getValue())
                        .add(loginName).add(loginName).add(loginName),
                UserSql.LOGIN_SQL).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService changePwd(long userId, String pwd, long versions, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(pwd).add(versions + 1).add(userId).add(versions), UserSql.CHANGE_PWD_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService getMemberById(long userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(userId).add(IsDeleted.NO.getValue()), UserSql.GET_USER_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService activateEmailUser(long userId, long versions, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(userId).add(versions), UserSql.ACTIVATE_EMAIL_USER_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService findEmailUser(String loginName, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(loginName), UserSql.ACTIVATE_EMAIL_USER_SELECT_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService getUserInfo(long userId, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(userId), UserSql.GET_USER_INFO_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService saveUserInfo(long userId, String loginName, String userName, String mobile, String email, int sex, long birthday, String photoUrl, Handler<AsyncResult<UpdateResult>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(userId), UserSql.GET_USER_INFO_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        Future future = userFuture.compose(user -> {
            Future updateFuture = Future.future();
            postgreSQLClient.rxGetConnection()
                    .flatMap(conn ->
                            conn.rxSetAutoCommit(false).toSingleDefault(false)
                                    .flatMap(autoCommit -> conn.rxUpdateWithParams(UserSql.UPDATE_USER_SQL,
                                            new JsonArray().add(loginName).add(userName).add(mobile).add(email).add(userId).add(user.getLong("versions"))))
                                    .flatMap(updateResult -> {
                                        if (Objects.isNull(user.getLong("info_versions"))) {
                                            return conn.rxUpdateWithParams(UserSql.INSERT_USER_INFO_SQL,
                                                    new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(userName).add(sex)
                                                            .add(DateFormatUtils.format(new Date(birthday), FormatUtils.DATE_FORMAT)).add(photoUrl));
                                        } else {
                                            return conn.rxUpdateWithParams(UserSql.UPDATE_USER_INFO_SQL,
                                                new JsonArray().add(userName).add(sex).add(DateFormatUtils.format(new Date(birthday), FormatUtils.DATE_FORMAT)).add(photoUrl)
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
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IUserService updateEmail(long userId, String email, long versions, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(email).add(userId).add(versions), UserSql.UPDATE_USER_EMAIL_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IUserService updateIdcard(long userId, String realName, String idCard, String idCardPositive, String idCardNegative, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(userId), UserSql.SELECT_USER_INFO_BY_USERID_SQL)
            .subscribe(future::complete, future::fail);
        future.compose(userInfo -> {
           if (Objects.isNull(userInfo) || userInfo.size() == 0) {
               Future<Integer> insertFuture = Future.future();
               this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(realName).add(idCard).add(idCardPositive).add(idCardNegative)
                       , UserSql.INSERT_USER_INFO_IDCARD_SQL)
                       .subscribe(insertFuture::complete, insertFuture::fail);
               return insertFuture;
           } else {
               Future<Integer> updateFuture = Future.future();
               this.execute(new JsonArray().add(realName).add(idCard).add(idCardPositive).add(idCardNegative).add(userId).add(userInfo.getLong("versions")),
                       UserSql.UPDATE_USER_IDCARD_SQL)
                       .subscribe(updateFuture::complete, updateFuture::fail);
               return updateFuture;
           }
        }).setHandler(handler);
        return this;
    }

    @Override
    public IUserService getIdcardInfo(long userId, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(userId), UserSql.GET_USER_IDCARD_INFO_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IUserService getBindMobile(long userId, Handler<AsyncResult<JsonObject>> handler) {
        return null;
    }

    @Override
    public IUserService bindMobile(long userId, String mobile, String code, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> userFuture = Future.future();
        this.getMemberById(userId, userFuture);
        IMessageService messageService = new ServiceProxyBuilder(vertx.getDelegate())
                .setAddress(IMessageService.MESSAGE_SERVICE_ADDRESS).build(IMessageService.class);
        userFuture.compose(user -> {
            if(JsonUtils.isNull(user)){
                LOGGER.error("【绑定手机号】查询用户【{}】信息不存在！", userId);
                return Future.failedFuture("用户信息不存在！");
            }
            Future<JsonObject> messageFuture = Future.future();
            messageService.findMessage(mobile, RegisterType.mobile, messageFuture);
            return messageFuture;
        }).compose(message -> {
            if(JsonUtils.isNull(message)){
                LOGGER.error("【绑定手机号】{}查询验证码信息不存在！", mobile);
                return Future.failedFuture("查询验证码信息不存在！");
            }
            if(!StringUtils.equals(code, message.getString("code"))){
                LOGGER.error("【绑定手机号】{}, 验证码【{}】不正确！", mobile, message.getString("code"));
                return Future.failedFuture("验证码错误！");
            }
            return Future.succeededFuture(code);
        }).compose(messageCode -> {
            JsonObject user = userFuture.result();
            Future<Integer> future = Future.future();
            this.execute(new JsonArray().add(mobile).add(userId).add(user.getLong("versions")), UserSql.BIND_MOBILE_SQL)
                    .subscribe(future::complete, future::fail);
            ICertifiedService certifiedService = new ServiceProxyBuilder(vertx.getDelegate())
                    .setAddress(ICertifiedService.CERTIFIED_SERVICE_ADDRESS).build(ICertifiedService.class);
            certifiedService.sendUserCertified(userId, CertifiedType.MOBILE_CERTIFIED.getKey(), mobile, certifiedHandler -> {});
            messageService.updateMessage(mobile, RegisterType.mobile, messageHandler -> {});
            return future;
        }).setHandler(handler);
        return this;
    }


}
