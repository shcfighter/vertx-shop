package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by za-wangshenhua on 2018/3/22.
 */
@ProxyGen
@VertxGen
public interface ICertifiedService {

    @Fluent
    ICertifiedService sendUserCertified(long userId, int certifiedType, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    ICertifiedService saveUserCertified(long userId, int certifiedType, long certifiedTime, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    ICertifiedService updateUserCertified(long certifiedId, long updateTime, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    ICertifiedService findUserCertifiedByUserId(long userId, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    ICertifiedService findUserCertifiedByUserIdAndType(long userId, int certifiedType, Handler<AsyncResult<JsonObject>> resultHandler);
}
