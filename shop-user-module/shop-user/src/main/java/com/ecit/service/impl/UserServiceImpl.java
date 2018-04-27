package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.enmu.IsDeleted;
import com.ecit.common.utils.FormatUtils;
import com.ecit.constants.UserSql;
import com.ecit.enmu.UserStatus;
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
}
