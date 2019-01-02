package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface IAccountHandler {

    static final String ACCOUNT_SERVICE_ADDRESS = "account-handler-address";

    @Fluent
    IAccountHandler insertAccount(long userId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAccountHandler findAccount(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IAccountHandler findAccountHandler(String token, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IAccountHandler payOrderHandler(String token, long orderId, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAccountHandler changePayPwdHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);
}
