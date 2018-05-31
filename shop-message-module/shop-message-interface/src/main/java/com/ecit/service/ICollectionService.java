package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.List;

/**
 * Created by za-wangshenhua on 2018/2/28.
 */
@VertxGen
@ProxyGen
public interface ICollectionService {

    static final String COLLECTION_SERVICE_ADDRESS = "collection_service_address";

    @Fluent
    ICollectionService sendCollection(JsonObject params, Handler<AsyncResult<String>> resultHandler);

    @Fluent
    ICollectionService findCollection(long userId, int page, Handler<AsyncResult<List<JsonObject>>> resultHandler);

    @Fluent
    ICollectionService updateCollection(long userId, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler);

}
