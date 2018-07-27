package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by shwang on 2018/3/22.
 */
@ProxyGen
@VertxGen
public interface ICertifiedHandler {

    public static final String CERTIFIED_SERVICE_ADDRESS = "certified-handler-address";

    @Fluent
    ICertifiedHandler sendUserCertified(long userId, int certifiedType, String remarks, Handler<AsyncResult<Void>> resultHandler);

    @Fluent
    ICertifiedHandler saveUserCertified(long userId, int certifiedType, String remarks, long certifiedTime, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    ICertifiedHandler updateUserCertified(long certifiedId, String remarks, long updateTime, Handler<AsyncResult<Integer>> resultHandler);

    @Fluent
    ICertifiedHandler findUserCertifiedByUserId(long userId, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    ICertifiedHandler findUserCertifiedByUserIdAndType(long userId, int certifiedType, Handler<AsyncResult<JsonObject>> resultHandler);
}
