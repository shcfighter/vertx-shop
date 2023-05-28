package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.FormatUtils;
import com.ecit.common.utils.JsonUtils;
import com.ecit.constants.UserSql;
import com.ecit.handler.ICertifiedHandler;
import com.ecit.handler.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.sqlclient.Tuple;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CertifiedHandler extends JdbcRxRepositoryWrapper implements ICertifiedHandler {

    private final static Logger LOGGER = LogManager.getLogger(CertifiedHandler.class);

    /**
     * kafka topic
     */
    private static final String CERTIFIED_TOPIC = "certified-topic";

    final KafkaConsumer<String, JsonObject> consumer;
    final KafkaProducer<String, JsonObject> producer;
    final Vertx vertx;

    public CertifiedHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;

        JsonObject kafkaConfig = config.getJsonObject("kafka");
        Map<String, String> producerConfig = (Map<String, String>) kafkaConfig.getJsonObject("producer.config");
        this.producer = KafkaProducer.createShared(vertx.getDelegate(), "the-producer", producerConfig);

        Map<String, String> consumerConfig = (Map<String, String>) kafkaConfig.getJsonObject("consumer.config");
        this.consumer = KafkaConsumer.create(vertx.getDelegate(), consumerConfig);

        consumer.subscribe(Set.of(CERTIFIED_TOPIC))
                .onSuccess(v ->{
                    this.saveCertified();
                    System.out.println("subscribed");
                }).onFailure(cause ->
                        System.out.println("Could not subscribe " + cause.getMessage())
                );
    }

    public void saveCertified() {
        this.consumer.handler(msg -> {
            JsonObject certified = msg.value();
            LOGGER.debug("Got certified message: {}", certified);
            if (certified.containsKey("user_id")) {
                Promise<JsonObject> oneCertifiedPromise = Promise.promise();
                this.findUserCertifiedByUserIdAndType(certified.getLong("user_id"), certified.getInteger("certified_type"), oneCertifiedPromise);
                oneCertifiedPromise.future().compose(oneCertified -> {
                    Promise<Integer> certifiedPromise = Promise.promise();
                    if (oneCertified.size() > 0) {
                        this.updateUserCertified(oneCertified.getLong("certified_id"), certified.getString("remarks"), certified.getLong("time"), certifiedPromise);
                    } else {
                        this.saveUserCertified(certified.getLong("user_id"), certified.getInteger("certified_type"), certified.getString("remarks"),
                                certified.getLong("time"), certifiedPromise);
                    }
                    return certifiedPromise.future();
                }).onComplete(handler -> {
                    if (handler.succeeded()) {

                    } else {
                        LOGGER.error("rabbitmq接收处理失败", handler.cause());
                    }
                });
            }
        });
    }

    /**
     * 发送rabbitmq
     * @param userId
     * @param certifiedType
     * @param resultHandler
     * @return
     */
    @Override
    public ICertifiedHandler sendUserCertified(long userId, int certifiedType, String remarks, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject message = new JsonObject().put("user_id", userId).put("certified_type", certifiedType)
                .put("time", System.currentTimeMillis()).put("remarks", remarks);
        Promise promise = Promise.promise();

        KafkaProducerRecord<String, JsonObject> record =
                KafkaProducerRecord.create(CERTIFIED_TOPIC, message);

        producer.send(record).onSuccess(recordMetadata -> promise.complete()).onFailure(promise::fail);

        promise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler saveUserCertified(long userId, int certifiedType, String remarks, long certifiedTime, Handler<AsyncResult<Integer>> resultHandler) {
        Promise<Integer> userPromise = Promise.promise();
        this.execute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(userId).addInteger(certifiedType)
                .addString(DateFormatUtils.format(new Date(certifiedTime), FormatUtils.DATE_TIME_MILLISECOND_FORMAT)).addString(remarks),
                UserSql.INSERT_USER_CERTIFIED_SQL)
                .subscribe(userPromise::complete, userPromise::fail);
        userPromise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler updateUserCertified(long certifiedId, String remarks, long updateTime, Handler<AsyncResult<Integer>> resultHandler) {
        Promise<Integer> userPromise = Promise.promise();
        this.execute(Tuple.tuple().addString(DateFormatUtils.format(new Date(updateTime), FormatUtils.DATE_TIME_MILLISECOND_FORMAT))
                .addString(remarks).addLong(certifiedId), UserSql.UPDATE_USER_CERTIFIED_SQL)
                .subscribe(userPromise::complete, userPromise::fail);
        userPromise.future().onComplete(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler findUserCertifiedByUserIdHandler(String token, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<List<JsonObject>> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            long userId = session.getLong("userId");
            Promise<List<JsonObject>> userPromise = Promise.promise();
            this.retrieveMany(Tuple.tuple().addLong(userId), UserSql.SELECT_USER_CERTIFIED_SQL)
                    .subscribe(userPromise::complete, userPromise::fail);
            return userPromise.future();
        });
        resultFuture.onComplete(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler findUserCertifiedByUserIdAndType(long userId, int certifiedType, Handler<AsyncResult<JsonObject>> resultHandler) {
        Promise<JsonObject> userPromise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addLong(userId).addInteger(certifiedType), UserSql.SELECT_USER_CERTIFIED_BY_TYPE_SQL)
                .subscribe(userPromise::complete, userPromise::fail);
        userPromise.future().onComplete(resultHandler);
        return this;
    }
}
