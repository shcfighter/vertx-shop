package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.List;

/**
 * Created by shwang on 2018/2/28.
 */
@VertxGen
@ProxyGen
public interface ICollectionHandler {

    static final String COLLECTION_SERVICE_ADDRESS = "collection_service_address";

    @Fluent
    ICollectionHandler sendCollection(JsonObject params, Handler<AsyncResult<String>> resultHandler);

    @Fluent
    ICollectionHandler findCollection(long userId, int page, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    ICollectionHandler updateCollection(long userId, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler);

}
