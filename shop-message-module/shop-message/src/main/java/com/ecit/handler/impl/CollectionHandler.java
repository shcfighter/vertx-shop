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
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * kafka topic
     */
    private static final String BROWSE_TOPIC = "browse-topic";
    private static final String COLLECTION_TOPIC = "collection-topic";

    final KafkaConsumer<String, JsonObject> consumer;
    final KafkaProducer<String, JsonObject> producer;
    final MongoClient mongoClient;
    final Vertx vertx;

    private ICommodityHandler commodityHandler;

    public CollectionHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
        this.commodityHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).build(ICommodityHandler.class);

        Map<String, String> producerConfig = new HashMap<>();
        producerConfig.put("bootstrap.servers", "127.0.0.1:9092");
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put("value.serializer", "io.vertx.kafka.client.serialization.JsonObjectSerializer");
        producerConfig.put("acks", "1");
        this.producer = KafkaProducer.createShared(vertx.getDelegate(), "the-producer", producerConfig);

        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.put("bootstrap.servers", "127.0.0.1:9092");
        consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
        consumerConfig.put("group.id", "preferences");
        consumerConfig.put("auto.offset.reset", "earliest");
        consumerConfig.put("enable.auto.commit", "true");
        this.consumer = KafkaConsumer.create(vertx.getDelegate(), consumerConfig);

        consumer.subscribe(Set.of(BROWSE_TOPIC, COLLECTION_TOPIC))
                .onSuccess(v ->{
                    this.consume();
                    System.out.println("subscribed");
                }).onFailure(cause ->
                        System.out.println("Could not subscribe " + cause.getMessage())
                );
    }

    private void consume() {
        consumer.handler(record -> {
            String topic = record.topic();
            JsonObject message = record.value();
            if (StringUtils.equals(topic, BROWSE_TOPIC)) {

            } else if (StringUtils.equals(topic, COLLECTION_TOPIC)) {

            }
        });
    }

    private void saveCollection(JsonObject collection) {
        LOGGER.debug("Got collection message: {}", collection);
        if (!JsonUtils.isNull(collection) && collection.containsKey("user_id")) {
            Promise<JsonObject> promise = Promise.promise();
            mongoClient.rxFindOne(MONGODB_COLLECTION, new JsonObject()
                            .put("user_id", collection.getLong("user_id"))
                            .put("commodity_id", collection.getLong("commodity_id")), null).subscribe(promise::complete, promise::fail);

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
    }

    private void saveBrowsingHistory(JsonObject browseJson) {
        LOGGER.debug("Got browse message: {}", browseJson);
        Long userId = browseJson.getLong("user_id");
        if (!JsonUtils.isNull(browseJson) && browseJson.containsKey("user_id")) {
            JsonObject document = new JsonObject().put("commodity", browseJson)
                    .put("commodity_id", browseJson.getLong("commodity_id"))
                    .put("user_id", userId).put("is_deleted", 0)
                    .put("create_time", System.currentTimeMillis());
            mongoClient.rxInsert(MONGODB_BROWSE, document).subscribe();
        }
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

            KafkaProducerRecord<String, JsonObject> record =
                    KafkaProducerRecord.create(COLLECTION_TOPIC, params);
            producer.send(record).onSuccess(recordMetadata -> promise.complete()).onFailure(promise::fail);
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

            KafkaProducerRecord<String, JsonObject> record =
                    KafkaProducerRecord.create(BROWSE_TOPIC, params);
            producer.send(record).onSuccess(recordMetadata -> promise.complete()).onFailure(promise::fail);
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
