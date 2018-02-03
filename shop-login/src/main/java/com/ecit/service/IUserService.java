package com.ecit.service;

import com.ecit.service.impl.UserServiceImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
@ProxyGen
@VertxGen
public interface IUserService {

    public static final String USER_SERVICE_ADDRESS = "user-service-address";

    @Fluent
    IUserService get(Handler<AsyncResult<JsonObject>> resultHandler);

}
