package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import com.ecit.common.enmu.IsDeleted;
import com.ecit.constants.UserSql;
import com.ecit.enmu.UserStatus;
import com.ecit.service.IUserService;
import com.ecit.service.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class UserServiceImpl extends JdbcRepositoryWrapper implements IUserService{

    private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class);
    public UserServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IUserService register(String loginName, String mobile, String email, String pwd, String salt,
                                 Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> future = Future.future();
        long userId = IdBuilder.getUniqueId();
        this.executeNoResult(new JsonArray().add(userId).add(StringUtils.isEmpty(loginName) ? "member_" + userId : loginName).add(pwd)
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
    public Future activateEmailUser(long userId, long versions) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(userId).add(versions), UserSql.ACTIVATE_EMAIL_USER_SQL)
                .subscribe(future::complete, future::fail);
        return future;
    }

    @Override
    public IUserService findEmailUser(String loginName, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(loginName), UserSql.ACTIVATE_EMAIL_USER_SELECT_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

}
