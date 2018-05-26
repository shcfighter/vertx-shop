package com.ecit.service.impl;

import com.ecit.service.ICollectionService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.List;

public class CollectionServiceImpl implements ICollectionService {

    static final String MONGODB_COLLECTION = "collection";

    private final static Logger LOGGER = LogManager.getLogger(CollectionServiceImpl.class);
    /**
     * rabbitmq 路由器
     */
    private static final String EXCHANGE = "vertx.shop.exchange";
    /**
     * rabbitmq routingkey
     */
    private static final String ROUTINGKEY = "collection";
    /**
     * rabbitmq 队列
     */
    private static final String QUEUES = "vertx.shop.collection.queues";
    /**
     * eventbus 地址
     */
    private static final String EVENTBUS_QUEUES = "eventbus.collection.queues";

    final RabbitMQClient rabbitMQClient;
    final MongoClient mongoClient;
    final Vertx vertx;

    public CollectionServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
        this.rabbitMQClient = RabbitMQClient.create(vertx, new RabbitMQOptions(config.getJsonObject("rabbitmq")));
        /**
         * 创建rabbitmq连接
         */
        rabbitMQClient.start(startMQ -> {
            if (startMQ.succeeded()) {
                LOGGER.info("rabbitmq start success !");
                this.saveCollection();
            } else {
                LOGGER.error("rabbitmq start failed !");
            }
        });
    }

    private void saveCollection() {
        vertx.eventBus().consumer(EVENTBUS_QUEUES,  msg -> {
            JsonObject json = (JsonObject) msg.body();
            LOGGER.debug("Got collection message: {}", json);
            JsonObject collection = new JsonObject(json.getString("body"));
            if (collection.containsKey("user_id")) {
                mongoClient.rxInsert(MONGODB_COLLECTION, collection).subscribe();
            }
        });
        rabbitMQClient.rxBasicConsume(QUEUES, EVENTBUS_QUEUES).subscribe();

    }

    @Override
    public ICollectionService sendCollection(JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        Future<String> future = Future.future();
        JsonObject message = new JsonObject().put("body", params.encodePrettily());
        rabbitMQClient.rxBasicPublish(EXCHANGE, ROUTINGKEY, message)
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICollectionService findCollection(long userId, int page, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        final int pageSize = 10;
        Future<List<JsonObject>> future = Future.future();
        JsonObject query = new JsonObject()
                .put("user_id", userId)
                .put("is_deleted", 0);
        mongoClient.rxFindWithOptions(MONGODB_COLLECTION, query, new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                .setSort(new JsonObject().put("create_time", -1)))
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICollectionService updateCollection(long userId, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        return this;
    }
}
