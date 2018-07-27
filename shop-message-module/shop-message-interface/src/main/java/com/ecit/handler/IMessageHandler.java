package com.ecit.handler;

import com.ecit.common.enums.RegisterType;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

/**
 * Created by shwang on 2018/2/28.
 */
@VertxGen
@ProxyGen
public interface IMessageHandler {

    static final String MESSAGE_SERVICE_ADDRESS = "message_service_address";

    static final String MONGODB_COLLECTION = "verification_code";

    @Fluent
    IMessageHandler saveMessage(String destination, RegisterType type, Handler<AsyncResult<String>> resultHandler);

    @Fluent
    IMessageHandler findMessage(String destination, RegisterType type, Handler<AsyncResult<JsonObject>> resultHandler);

    @Fluent
    IMessageHandler updateMessage(String destination, RegisterType type, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler);

    @Fluent
    IMessageHandler registerEmailMessage(String destination, Handler<AsyncResult<String>> resultHandler);

    @Fluent
    IMessageHandler registerMobileMessage(String destination, Handler<AsyncResult<String>> resultHandler);
}
