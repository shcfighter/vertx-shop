package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import com.ecit.service.IUserService;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class UserServiceImpl /*extends JdbcRepositoryWrapper*/ implements IUserService{


    public UserServiceImpl(Vertx vertx, JsonObject config) {
        //super(vertx, config);
    }

    @Override
    public IUserService get(Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> future = Future.future();
        Single.just(new JsonObject().put("name", "张三")).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }
}
