package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.handler.ICollectionHandler;
import com.ecit.handler.ICommodityHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CollectionHandler extends JdbcRxRepositoryWrapper implements ICollectionHandler {
    private final static Logger LOGGER = LogManager.getLogger(CollectionHandler.class);

    /**
     * 收藏商品 mongodb key
     */
    static final String MONGODB_COLLECTION = "collection";

    /**
     * 浏览商品历史记录 mongodb key
     */
    static final String MONGODB_BROWSE = "browse";

    /**
     * rabbitmq 路由器
     */
    private static final String EXCHANGE = "vertx.shop.exchange";
    /**
     * rabbitmq routingkey
     */
    private static final String ROUTINGKEY = "collection";
    /**
     * rabbitmq routingkey
     */
    private static final String BROWSE_ROUTINGKEY = "browse";
    /**
     * rabbitmq 队列
     */
    private static final String QUEUES = "vertx.shop.collection.queues";
    /**
     * rabbitmq 队列
     */
    private static final String BROWSE_QUEUES = "vertx.shop.browse.queues";
    /**
     * eventbus 地址
     */
    private static final String EVENTBUS_QUEUES = "eventbus.collection.queues";
    /**
     * browse eventbus 地址
     */
    private static final String BROWSE_EVENTBUS_QUEUES = "eventbus.browse.queues";

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
                this.saveBrowsingHistory();
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
                Promise<JsonObject> promise = Promise.promise();
                mongoClient.rxFindOne(MONGODB_COLLECTION, new JsonObject()
                                .put("user_id", collection.getLong("user_id"))
                                .put("commodity_id", collection.getLong("commodity_id")), null).subscribe(promise::complete, promise::fail);
                /*promise.compose(o -> {
                    if (JsonUtils.isNull(o)) {
                        LOGGER.info("商品不存在！");
                    } else {
                        LOGGER.info("123456789: {}", o);
                    }
                    return Future.succeededFuture();
                });*/
                mongoClient.findOne(MONGODB_COLLECTION, new JsonObject()
                        .put("user_id", collection.getLong("user_id"))
                        .put("commodity_id", collection.getLong("commodity_id")), null, collectionHandler -> {
                    if (collectionHandler.failed()) {
                        LOGGER.error(collectionHandler.cause());
                        return ;
                    }
                    JsonObject collectionJson = collectionHandler.result();
                    if(JsonUtils.isNull(collectionJson)){
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
                    } else {
                        LOGGER.info("商品已经被收藏！");
                    }
                });
            }
        });
        rabbitMQClient.rxBasicConsumer(QUEUES).subscribe();
    }

    private void saveBrowsingHistory() {
        vertx.eventBus().consumer(BROWSE_EVENTBUS_QUEUES,  msg -> {
            JsonObject json = (JsonObject) msg.body();
            LOGGER.debug("Got browse message: {}", json);
            JsonObject browseJson = new JsonObject(json.getString("body"));
            Long userId = browseJson.getLong("user_id");
            if (!JsonUtils.isNull(browseJson) && browseJson.containsKey("user_id")) {
                JsonObject document = new JsonObject().put("commodity", browseJson)
                        .put("commodity_id", browseJson.getLong("commodity_id"))
                        .put("user_id", userId).put("is_deleted", 0)
                        .put("create_time", System.currentTimeMillis());
                mongoClient.rxInsert(MONGODB_BROWSE, document).subscribe();
            }
        });
        rabbitMQClient.rxBasicConsumer(BROWSE_QUEUES).subscribe();
    }

    @Override
    public ICollectionHandler sendCollection(String token, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<String> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            JsonObject session = sessionResult.result();
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                promise.fail("can not get session");
            }
            params.put("type", MONGODB_COLLECTION).put("user_id", session.getLong("userId"));

            JsonObject message = new JsonObject().put("body", params.encodePrettily());
            rabbitMQClient.rxBasicPublish(EXCHANGE, ROUTINGKEY, Buffer.buffer(message.encodePrettily()))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(resultHandler);
        return this;
    }

    @Override
    public ICollectionHandler sendBrowse(String token, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<String> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            JsonObject session = sessionResult.result();
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                promise.fail("can not get session");
            }
            params.put("type", MONGODB_BROWSE).put("user_id", session.getLong("userId"));

            JsonObject message = new JsonObject().put("body", params.encodePrettily());
            rabbitMQClient.rxBasicPublish(EXCHANGE, BROWSE_ROUTINGKEY, Buffer.buffer(message.encodePrettily()))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(resultHandler);
        return this;
    }

    @Override
    public ICollectionHandler findCollection(String token, int page, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        final int pageSize = 8;
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<List<JsonObject>> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            JsonObject session = sessionResult.result();
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                promise.fail("can not get session");
            }
            final long userId = session.getLong("userId");

            JsonObject query = new JsonObject()
                    .put("user_id", userId)
                    .put("is_deleted", 0);
            mongoClient.rxFindWithOptions(MONGODB_COLLECTION, query, new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                    .setSort(new JsonObject().put("create_time", -1)))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(resultHandler);
        return this;
    }

    @Override
    public ICollectionHandler updateCollection(long userId, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        return this;
    }

    @Override
    public ICollectionHandler removeCollection(String token, String id, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<MongoClientUpdateResult> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            JsonObject session = sessionResult.result();
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                promise.fail("can not get session");
            }
            final long userId = session.getLong("userId");

            JsonObject query = new JsonObject()
                    .put("_id", id)
                    .put("user_id", userId)
                    .put("is_deleted", 0);
            JsonObject update = new JsonObject().put("$set", new JsonObject().put("is_deleted", 1));
            mongoClient.rxUpdateCollectionWithOptions(MONGODB_COLLECTION, query, update, new UpdateOptions().setMulti(true))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(resultHandler);
        return this;
    }

    @Override
    public ICollectionHandler findBrowsingHistory(String token, int page, int pageSize, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<List<JsonObject>> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            long userId = sessionResult.result().getLong("user_id");
            JsonObject query = new JsonObject().put("user_id", userId).put("is_deleted", 0);
            mongoClient.rxFindWithOptions(MONGODB_BROWSE, query, new FindOptions().setLimit(pageSize).setSkip(((page - 1) * pageSize))
                    .setSort(new JsonObject().put("create_time", -1)))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(handler);
        return this;
    }

    @Override
    public ICollectionHandler rowNumBrowsingHistory(String token, Handler<AsyncResult<Long>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<Long> promise = Promise.promise();
        sessionFuture.andThen(session -> {
            long userId = session.result().getLong("user_id");
            mongoClient.rxCount(MONGODB_BROWSE, new JsonObject().put("user_id", userId))
                    .subscribe(promise::complete, promise::fail);
        });
        promise.future().andThen(handler);
        return this;
    }
}
