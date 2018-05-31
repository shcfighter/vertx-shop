package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface IAccountService {

    static final String ACCOUNT_SERVICE_ADDRESS = "account-service-address";

    @Fluent
    IAccountService insertAccount(long userId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAccountService findAccount(long userId, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    IAccountService payOrder(long userId, long orderId, String payPwd, Handler<AsyncResult<Integer>> handler);
}
