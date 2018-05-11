package com.ecit.service;

import io.vertx.codegen.annotations.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

import java.util.Date;
import java.util.List;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
@ProxyGen
@VertxGen
public interface IUserService {

    public static final String USER_SERVICE_ADDRESS = "user-service-address";

    @Fluent
    IUserService register(long userId, String loginName, String mobile, String email, String pwd, String salt, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    IUserService login(String loginName, String pwd, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserService changePwd(long userId, String pwd, long versions, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    IUserService getMemberById(long userId, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserService activateEmailUser(long userId, long versions, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    IUserService findEmailUser(String loginName, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserService getUserInfo(long userId, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserService saveUserInfo(long userId, String loginName, String userName, String mobile, String email, int sex, long birthday, String photoUrl, Handler<AsyncResult<UpdateResult>> resultHandler);

    @Fluent
    IUserService updateEmail(long userId, String email, long versions, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserService updateIdcard(long userId, String realName, String idCard, String idCardPositive, String idCardNegative, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserService getIdcardInfo(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IUserService getBindMobile(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IUserService bindMobile(long userId, String mobile, String code, Handler<AsyncResult<Integer>> handler);


}
