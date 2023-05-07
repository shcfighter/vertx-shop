package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;

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
    IUserHandler getUserInfoHandler(String token, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IUserHandler saveUserInfoHandler(String token, JsonObject params, Handler<AsyncResult<RowSet<Row>>> resultHandler);

    @Fluent
    IUserHandler updateEmail(long userId, String email, long versions, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler updateIdcardHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler getIdcardInfoHandler(String token, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IUserHandler getBindMobile(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IUserHandler bindMobileHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler changePwdHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IUserHandler changeEmailHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);
}
