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
    IAccountHandler payOrder(long userId, long orderId, String payPwd, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAccountHandler changePayPwd(long userId, String payPwd, String code, Handler<AsyncResult<Integer>> handler);
}
