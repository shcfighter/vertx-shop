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
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by shwang on 2018/3/22.
 */
public class PreferencesHandler implements IPreferencesHandler {

    final MongoClient mongoClient;
    final KafkaConsumer<String, JsonObject> consumer;
    final KafkaProducer<String, JsonObject> producer;
    final Vertx vertx;
    private final static Logger LOGGER = LoggerFactory.getLogger(PreferencesHandler.class);
    /**
     * mongodb的collection名称
     */
    private static final String PREFERENCES_COLLECTION = "preferences";
    /**
     * kafka topic
     */
    private static final String PREFERENCES_TOPIC = "preferences-topic";


    public PreferencesHandler(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));

        JsonObject kafkaConfig = config.getJsonObject("kafka");
        Map<String, String> producerConfig = (Map<String, String>) kafkaConfig.getJsonObject("producer.config");
        this.producer = KafkaProducer.createShared(vertx.getDelegate(), "the-producer", producerConfig);

        Map<String, String> consumerConfig = (Map<String, String>) kafkaConfig.getJsonObject("consumer.config");
        this.consumer = KafkaConsumer.create(vertx.getDelegate(), consumerConfig);

        consumer.subscribe(Set.of(PREFERENCES_TOPIC))
            .onSuccess(v ->{
                this.savePreferences(handler -> {});
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
        this.consumer.handler(record -> {
            JsonObject perferences = record.value();
            LOGGER.debug("Got perferences message: {}", perferences);
            mongoClient.rxInsert(PREFERENCES_COLLECTION, perferences)
                    .subscribe();
        });
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
        KafkaProducerRecord<String, JsonObject> record =
                KafkaProducerRecord.create(PREFERENCES_TOPIC, new JsonObject().put("cookies", cookies)
                        .put("keyword", keyword)
                        .put("search_type", searchType.name())
                        .put("create_time", new Date().getTime()));

        producer.send(record).onSuccess(recordMetadata -> promise.complete()).onFailure(promise::fail);

        promise.future().onComplete(handler);
        return this;
    }
}
