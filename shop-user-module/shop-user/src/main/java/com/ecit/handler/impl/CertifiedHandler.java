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
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import io.vertx.reactivex.sqlclient.Tuple;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;

public class CertifiedHandler extends JdbcRxRepositoryWrapper implements ICertifiedHandler {

    private final static Logger LOGGER = LogManager.getLogger(CertifiedHandler.class);
    /**
     * rabbitmq 路由器
     */
    private static final String EXCHANGE = "vertx.shop.exchange";
    /**
     * rabbitmq routingkey
     */
    private static final String ROUTINGKEY = "certified";
    /**
     * rabbitmq 队列
     */
    private static final String QUEUES = "vertx.shop.certified.queues";
    /**
     * eventbus 地址
     */
    private static final String EVENTBUS_QUEUES = "eventbus.certified.queues";

    final RabbitMQClient rabbitMQClient;
    final Vertx vertx;

    public CertifiedHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        rabbitMQClient = RabbitMQClient.create(vertx, new RabbitMQOptions(config.getJsonObject("rabbitmq")));
        /**
         * 创建rabbitmq连接
         */
        rabbitMQClient.start(startMQ -> {
            if (startMQ.succeeded()) {
                LOGGER.info("rabbitmq start success !");
                this.saveCertified();
            } else {
                LOGGER.error("rabbitmq start failed !");
            }
        });
    }

    public void saveCertified() {
        vertx.eventBus().consumer(EVENTBUS_QUEUES,  msg -> {
            JsonObject json = (JsonObject) msg.body();
            LOGGER.debug("Got certified message: {}", json);
            JsonObject certified = new JsonObject(json.getString("body"));
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
                        // ack
                        rabbitMQClient.basicAck(json.getLong("deliveryTag"), false, asyncResult -> {
                            if (asyncResult.failed()) {
                                LOGGER.error("rabbitmq接收确认失败", asyncResult.cause());
                            }
                        });
                    } else {
                        LOGGER.error("rabbitmq接收处理失败", handler.cause());
                    }
                });
            }
        });

        // Setup the link between rabbitmq consumer and event bus address
        rabbitMQClient.getDelegate().basicConsumer(QUEUES, rabbitMQConsumerAsyncResult -> {
            if (rabbitMQConsumerAsyncResult.succeeded()) {
                System.out.println("RabbitMQ consumer created !");
                RabbitMQConsumer mqConsumer = rabbitMQConsumerAsyncResult.result();
                mqConsumer.handler(message -> {
                    System.out.println("Got message: " + message.body().toString());
                }).exceptionHandler(err -> {
                    System.out.println("consume error " + err.getMessage());
                });
            } else {
                rabbitMQConsumerAsyncResult.cause().printStackTrace();
            }
        });
        return ;
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
        JsonObject message = new JsonObject().put("body", new JsonObject().put("user_id", userId).put("certified_type", certifiedType)
                .put("time", System.currentTimeMillis()).put("remarks", remarks).encodePrettily());
        Promise promise = Promise.promise();
        // Put the channel in confirm mode. This can be done once at init.
        rabbitMQClient.confirmSelect(confirmResult -> {
            if(confirmResult.succeeded()) {
                rabbitMQClient.basicPublish(EXCHANGE, ROUTINGKEY, Buffer.buffer(message.encodePrettily()), pubResult -> {
                    if (pubResult.succeeded()) {
                        // Check the message got confirmed by the broker.
                        rabbitMQClient.waitForConfirms(waitResult -> {
                            if(waitResult.succeeded()){
                                promise.complete();
                                LOGGER.info("Message published ! {}", message);
                            }
                            else{
                                promise.fail(waitResult.cause());
                                LOGGER.error("rabbitmq 确认发送异常", waitResult.cause());
                            }
                        });
                    } else {
                        promise.fail(pubResult.cause());
                        LOGGER.error("rabbitmq 发送异常!", pubResult.cause());
                    }
                });
            } else {
                promise.fail(confirmResult.cause());
                LOGGER.error("rabbitmq 连接异常", confirmResult.cause());
            }
        });
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
