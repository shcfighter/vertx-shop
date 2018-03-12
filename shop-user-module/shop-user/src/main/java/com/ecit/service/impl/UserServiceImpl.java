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

import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class UserServiceImpl extends JdbcRepositoryWrapper implements IUserService{


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
    public IUserService activateEmailUser(String loginName, Handler<AsyncResult<Integer>> resultHandler) {
        Future<JsonObject> selectFuture = Future.future();
        this.retrieveOne(new JsonArray().add(loginName), UserSql.SELECT_ACTIVATE_EMAIL_USER_SQL)
                .subscribe(selectFuture::complete, selectFuture::fail);
        if(selectFuture.succeeded()){
            //todo
        } else {
            //todo
        }
        Future<Integer> future = Future.future();

        future.setHandler(resultHandler);
        return this;
    }
}
