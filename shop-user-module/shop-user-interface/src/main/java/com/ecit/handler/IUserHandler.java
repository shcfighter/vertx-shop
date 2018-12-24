package com.ecit.handler;

import com.ecit.common.utils.salt.ShopHashStrategy;
import io.vertx.codegen.annotations.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

/**
 * Created by shwang on 2018/2/2.
 */
@ProxyGen
@VertxGen
public interface IUserHandler {

    public static final String USER_SERVICE_ADDRESS = "user-handler-address";

    @Fluent
    IUserHandler register(long userId, String loginName, String mobile, String email, String pwd, String salt, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    IUserHandler login(String loginName, String pwd, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserHandler changePwd(long userId, String pwd, long versions, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    IUserHandler getMemberById(long userId, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserHandler activateEmailUser(long userId, long versions, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    IUserHandler findEmailUser(String loginName, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserHandler getUserInfo(long userId, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserHandler saveUserInfo(long userId, String loginName, String userName, String mobile, String email, int sex, long birthday, String photoUrl, Handler<AsyncResult<UpdateResult>> resultHandler);

    @Fluent
    IUserHandler updateEmail(long userId, String email, long versions, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler updateIdcard(long userId, String realName, String idCard, String idCardPositive, String idCardNegative, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler getIdcardInfo(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IUserHandler getBindMobile(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IUserHandler bindMobile(long userId, String mobile, String code, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler changePwd(String token, JsonObject params, ShopHashStrategy hashStrategy, Handler<AsyncResult<Integer>> handler);
}
