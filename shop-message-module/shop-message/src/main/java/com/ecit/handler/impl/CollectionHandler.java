package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.id.IdWorker;
import com.ecit.common.utils.JsonUtils;
import com.ecit.handler.ICollectionHandler;
import com.ecit.handler.ICommodityHandler;
import com.ecit.handler.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CollectionHandler extends JdbcRxRepositoryWrapper implements ICollectionHandler {

    static final String MONGODB_COLLECTION = "collection";

    private final static Logger LOGGER = LogManager.getLogger(CollectionHandler.class);
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

    private ICommodityHandler commodityHandler;

    public CollectionHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
        this.rabbitMQClient = RabbitMQClient.create(vertx, new RabbitMQOptions(config.getJsonObject("rabbitmq")));
        this.commodityHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).build(ICommodityHandler.class);
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
            if (!JsonUtils.isNull(collection) && collection.containsKey("user_id")) {
                commodityHandler.findCommodityById(collection.getLong("commodity_id"), handler -> {
                   if (handler.failed()) {
                       LOGGER.error("collection fail, get commodity error: ", handler.cause());
                       return ;
                   }
                   JsonObject commodity = handler.result();
                   mongoClient.rxInsert(MONGODB_COLLECTION, collection
                           .put("brand_name", commodity.getString("brand_name"))
                           .put("category_name", commodity.getString("category_name"))
                           .put("commodity_name", commodity.getString("commodity_name"))
                           .put("price", commodity.getString("price"))
                           .put("original_price", commodity.getString("original_price"))
                           .put("image_url", commodity.getString("image_url"))).subscribe();
                });
            }
        });
        rabbitMQClient.rxBasicConsume(QUEUES, EVENTBUS_QUEUES).subscribe();

    }

    @Override
    public ICollectionHandler sendCollection(String token, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            params.put("user_id", session.getLong("userId"));
            Future<String> future = Future.future();
            JsonObject message = new JsonObject().put("body", params.encodePrettily());
            rabbitMQClient.rxBasicPublish(EXCHANGE, ROUTINGKEY, message)
                    .subscribe(future::complete, future::fail);
            return future;
        }).setHandler(resultHandler);
        return this;
    }

    @Override
    public ICollectionHandler findCollection(String token, int page, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        final int pageSize = 8;
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<List<JsonObject>> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Future<List<JsonObject>> future = Future.future();
            JsonObject query = new JsonObject()
                    .put("user_id", userId)
                    .put("is_deleted", 0);
            mongoClient.rxFindWithOptions(MONGODB_COLLECTION, query, new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                    .setSort(new JsonObject().put("create_time", -1)))
                    .subscribe(future::complete, future::fail);
            return future;
        });
        resultFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICollectionHandler updateCollection(long userId, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        return this;
    }

    @Override
    public ICollectionHandler removeCollection(String token, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Future<MongoClientUpdateResult> future = Future.future();
            JsonObject query = new JsonObject()
                    .put("_id", id)
                    .put("user_id", userId)
                    .put("is_deleted", 0);
            JsonObject update = new JsonObject().put("$set", new JsonObject().put("is_deleted", 1));
            mongoClient.rxUpdateCollectionWithOptions(MONGODB_COLLECTION, query, update, new UpdateOptions().setMulti(true))
                    .subscribe(future::complete, future::fail);
            return future;
        }).setHandler(resultHandler);
        return this;
    }
}
