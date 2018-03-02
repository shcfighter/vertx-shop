package com.ecit.service.impl;

import com.ecit.common.enmu.RegisterType;
import com.ecit.service.IMessageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by za-wangshenhua on 2018/2/28.
 */
public class MessageServiceImpl implements IMessageService{

    private static final Logger LOGGER = LogManager.getLogger(MessageServiceImpl.class);
    private static MongoClient mongoClient;

    public MessageServiceImpl(Vertx vertx, JsonObject config) {
        mongoClient = MongoClient.createShared(vertx, config, "message");
    }

    @Override
    public IMessageService saveMessage(String destination, RegisterType type, Handler<AsyncResult<String>> resultHandler) {
        JsonObject document = new JsonObject()
                .put("type", type.name())
                .put("destination", destination)
                .put("code", (int) ((Math.random()*9+1)*100000))
                .put("status", 0)
                .put("createTime", new Date().getTime());

        Future<String> future = Future.future();
        mongoClient.rxInsert(MONGODB_COLLECTION, document).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IMessageService findMessage(String destination, RegisterType type, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject query = new JsonObject()
                .put("type", type.name())
                .put("destination", destination)
                .put("status", 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 5);
        query.put("createTime", new JsonObject().put("$gte", calendar.getTime().getTime()));
        Future<JsonObject> future = Future.future();
        mongoClient.rxFindOne(MONGODB_COLLECTION, query, null).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IMessageService updateMessage(String destination, RegisterType type, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        JsonObject query = new JsonObject()
                .put("type", type.name())
                .put("destination", destination);
        JsonObject update = new JsonObject()
                .put("$set", new JsonObject()
                        .put("status", 1)
                        .put("updateTime", new Date().getTime()));
        Future<MongoClientUpdateResult> future = Future.future();
        mongoClient.rxUpdateCollectionWithOptions(MONGODB_COLLECTION, query, update, new UpdateOptions()
                .setMulti(true))
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }
}
