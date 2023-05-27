package com.ecit.handler.impl;

import com.ecit.SearchType;
import com.ecit.handler.IPreferencesHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by shwang on 2018/3/22.
 */
public class PreferencesHandler implements IPreferencesHandler {

    final MongoClient mongoClient;
    final KafkaConsumer<String, String> consumer;
    final KafkaProducer<String, String> producer;
    final Vertx vertx;
    private final static Logger LOGGER = LogManager.getLogger(PreferencesHandler.class);
    /**
     * mongodb的collection名称
     */
    private static final String PREFERENCES_COLLECTION = "preferences";
    /**
     * rabbitmq 路由器
     */
    private static final String EXCHANGE = "vertx.shop.exchange";
    /**
     * rabbitmq routingkey
     */
    private static final String ROUTINGKEY = "preferences";
    /**
     * rabbitmq 队列
     */
    private static final String QUEUES = "vertx.shop.preferences.queues";
    /**
     * eventbus 地址
     */
    private static final String EVENTBUS_QUEUES = "eventbus.preferences.queues";
    private static final String PREFERENCES_TOPIC = "preferences-topic";


    public PreferencesHandler(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));

        Map<String, String> producerConfig = new HashMap<>();
        producerConfig.put("bootstrap.servers", "127.0.0.1:9092");
        producerConfig.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerConfig.put("acks", "1");
        producer = KafkaProducer.createShared(vertx.getDelegate(), "the-producer", producerConfig);

        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.put("bootstrap.servers", "127.0.0.1:9092");
        consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("group.id", "ttttt");
        consumerConfig.put("auto.offset.reset", "earliest");
        consumerConfig.put("enable.auto.commit", "true");

// use consumer for interacting with Apache Kafka
        this.consumer = KafkaConsumer.create(vertx.getDelegate(), consumerConfig);

        consumer.subscribe(Set.of(PREFERENCES_TOPIC))
            .onSuccess(v ->{
                System.out.println("subscribed");
            }).onFailure(cause ->
                    System.out.println("Could not subscribe " + cause.getMessage())
            );
    }

    /**
     * 保存查询关键字
     * @return
     */
    @Override
    public IPreferencesHandler savePreferences(Handler<AsyncResult<Void>> handler) {
        vertx.eventBus().consumer(EVENTBUS_QUEUES, msg -> {
            JsonObject json = (JsonObject) msg.body();
            JsonObject perferences = new JsonObject(json.getString("body"));
            LOGGER.debug("Got perferences message: {}", perferences);
            mongoClient.rxInsert(PREFERENCES_COLLECTION, new JsonObject(json.getString("body")))
                    .subscribe();
        });

        // Setup the link between rabbitmq consumer and event bus address
        rabbitMQClient.rxBasicConsumer(QUEUES, EVENTBUS_QUEUES).subscribe();



        return this;
    }

    /**
     * 历史查询关键字
     * @param cookies
     * @param handler
     * @return
     */
    @Override
    public IPreferencesHandler findPreferences(String cookies, Handler<AsyncResult<List<String>>> handler) {
        Promise<List<String>> promise = Promise.promise();
        mongoClient.rxFindWithOptions(PREFERENCES_COLLECTION, new JsonObject().put("cookies", cookies),
                new FindOptions().setLimit(3).setSort(new JsonObject().put("create_time", -1)))
                .subscribe(res -> promise.complete(res.stream().map(jsonObject -> jsonObject.getString("keyword")).collect(Collectors.toList())), promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 将用户偏好发送至mq
     * @param cookies
     * @param keyword
     * @param searchType
     * @param handler
     * @return
     */
    @Override
    public IPreferencesHandler sendMqPreferences(String cookies, String keyword, SearchType searchType, Handler<AsyncResult<String>> handler) {
        Promise<String> promise = Promise.promise();
        if(StringUtils.isEmpty(cookies)){
            promise.fail("cookies 为null");
            promise.future().onComplete(handler);
            return this;
        }
        KafkaProducerRecord<String, String> record =
                KafkaProducerRecord.create(PREFERENCES_TOPIC, new JsonObject().put("cookies", cookies)
                        .put("keyword", keyword)
                        .put("search_type", searchType.name())
                        .put("create_time", new Date().getTime()).encodePrettily());

        producer.send(record).onSuccess(recordMetadata -> promise.complete()).onFailure(promise::fail);

        promise.future().onComplete(handler);
        return this;
    }
}
