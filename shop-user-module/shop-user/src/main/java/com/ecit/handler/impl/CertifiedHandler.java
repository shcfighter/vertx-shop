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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
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
                Future<JsonObject> oneCertifiedFuture = Future.future();
                this.findUserCertifiedByUserIdAndType(certified.getLong("user_id"), certified.getInteger("certified_type"), oneCertifiedFuture);
                oneCertifiedFuture.compose(oneCertified -> {
                    Future<Integer> certifiedFuture = Future.future();
                    if (oneCertified.size() > 0) {
                        this.updateUserCertified(oneCertified.getLong("certified_id"), certified.getString("remarks"), certified.getLong("time"), certifiedFuture);
                    } else {
                        this.saveUserCertified(certified.getLong("user_id"), certified.getInteger("certified_type"), certified.getString("remarks"),
                                certified.getLong("time"), certifiedFuture);
                    }
                    return certifiedFuture;
                }).setHandler(handler -> {
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
        rabbitMQClient.rxBasicConsume(QUEUES, EVENTBUS_QUEUES, false).subscribe();
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
        Future future = Future.future();
        // Put the channel in confirm mode. This can be done once at init.
        rabbitMQClient.confirmSelect(confirmResult -> {
            if(confirmResult.succeeded()) {
                rabbitMQClient.basicPublish(EXCHANGE, ROUTINGKEY, message, pubResult -> {
                    if (pubResult.succeeded()) {
                        // Check the message got confirmed by the broker.
                        rabbitMQClient.waitForConfirms(waitResult -> {
                            if(waitResult.succeeded()){
                                future.complete();
                                LOGGER.info("Message published ! {}", message);
                            }
                            else{
                                future.failed();
                                LOGGER.error("rabbitmq 确认发送异常", waitResult.cause());
                            }
                        });
                    } else {
                        future.failed();
                        LOGGER.error("rabbitmq 发送异常!", pubResult.cause());
                    }
                });
            } else {
                future.failed();
                LOGGER.error("rabbitmq 连接异常", confirmResult.cause());
            }
        });
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler saveUserCertified(long userId, int certifiedType, String remarks, long certifiedTime, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> userFuture = Future.future();
        this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(certifiedType)
                .add(DateFormatUtils.format(new Date(certifiedTime), FormatUtils.DATE_TIME_MILLISECOND_FORMAT)).add(remarks),
                UserSql.INSERT_USER_CERTIFIED_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler updateUserCertified(long certifiedId, String remarks, long updateTime, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> userFuture = Future.future();
        this.execute(new JsonArray().add(DateFormatUtils.format(new Date(updateTime), FormatUtils.DATE_TIME_MILLISECOND_FORMAT))
                .add(remarks).add(certifiedId), UserSql.UPDATE_USER_CERTIFIED_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
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
            Future<List<JsonObject>> userFuture = Future.future();
            this.retrieveMany(new JsonArray().add(userId), UserSql.SELECT_USER_CERTIFIED_SQL)
                    .subscribe(userFuture::complete, userFuture::fail);
            return userFuture;
        });
        resultFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICertifiedHandler findUserCertifiedByUserIdAndType(long userId, int certifiedType, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(userId).add(certifiedType), UserSql.SELECT_USER_CERTIFIED_BY_TYPE_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }
}
