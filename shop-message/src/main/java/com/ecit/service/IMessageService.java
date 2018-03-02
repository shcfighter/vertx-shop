package com.ecit.service;

import com.ecit.common.enmu.RegisterType;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;

/**
 * Created by za-wangshenhua on 2018/2/28.
 */
@VertxGen
@ProxyGen
public interface IMessageService {

    static final String MESSAGE_SERVICE_ADDRESS = "message_service_address";

    static final String MONGODB_COLLECTION = "verification_code";

    @Fluent
    IMessageService saveMessage(String destination, RegisterType type, Handler<AsyncResult<String>> resultHandler);

    @Fluent
    IMessageService findMessage(String destination, RegisterType type, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IMessageService updateMessage(String destination, RegisterType type, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler);
}
