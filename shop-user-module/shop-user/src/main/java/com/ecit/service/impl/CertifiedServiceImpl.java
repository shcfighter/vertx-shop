package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.constants.UserSql;
import com.ecit.service.ICertifiedService;
import com.ecit.service.IUserService;
import com.ecit.service.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;

public class CertifiedServiceImpl extends JdbcRxRepositoryWrapper implements ICertifiedService {

    private final static Logger LOGGER = LogManager.getLogger(CertifiedServiceImpl.class);
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

    final RabbitMQClient rabbitMQClient;
    final Vertx vertx;
    final IUserService userService;

    public CertifiedServiceImpl(Vertx vertx, JsonObject config, IUserService userService) {
        super(vertx, config);
        this.vertx = vertx;
        this.userService = userService;
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
        vertx.eventBus().consumer("my.address",  msg -> {
            JsonObject json = (JsonObject) msg.body();
            LOGGER.debug("Got certified message: {}", json);
            JsonObject certified = new JsonObject(json.getString("body"));
            if (certified.containsKey("user_id")) {
                Future<JsonObject> oneCertifiedFuture = Future.future();
                this.findUserCertifiedByUserIdAndType(certified.getLong("user_id"), certified.getInteger("certified_type"), oneCertifiedFuture);
                oneCertifiedFuture.compose(oneCertified -> {
                    Future<Integer> certifiedFuture = Future.future();
                    if (oneCertified.size() > 0) {
                        this.updateUserCertified(oneCertified.getLong("certified_id"), certifiedFuture);
                    } else {
                        this.saveUserCertified(certified.getLong("user_id"), certified.getInteger("certified_type"), certifiedFuture);
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
        rabbitMQClient.rxBasicConsume(QUEUES, "my.address", false).subscribe();
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
    public ICertifiedService sendUserCertified(long userId, int certifiedType, Handler<AsyncResult<Void>> resultHandler) {
        JsonObject message = new JsonObject().put("body", new JsonObject().put("user_id", userId).put("certified_type", certifiedType).encodePrettily());
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
    public ICertifiedService saveUserCertified(long userId, int certifiedType, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> userFuture = Future.future();
        this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId).add(certifiedType), UserSql.INSERT_USER_CERTIFIED_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICertifiedService updateUserCertified(long certifiedId, Handler<AsyncResult<Integer>> resultHandler) {
        Future<Integer> userFuture = Future.future();
        this.execute(new JsonArray().add(certifiedId), UserSql.UPDATE_USER_CERTIFIED_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICertifiedService findUserCertifiedByUserId(long userId, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
        Future<List<JsonObject>> userFuture = Future.future();
        this.retrieveMany(new JsonArray().add(userId), UserSql.SELECT_USER_CERTIFIED_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }

    @Override
    public ICertifiedService findUserCertifiedByUserIdAndType(long userId, int certifiedType, Handler<AsyncResult<JsonObject>> resultHandler) {
        Future<JsonObject> userFuture = Future.future();
        this.retrieveOne(new JsonArray().add(userId).add(certifiedType), UserSql.SELECT_USER_CERTIFIED_BY_TYPE_SQL)
                .subscribe(userFuture::complete, userFuture::fail);
        userFuture.setHandler(resultHandler);
        return this;
    }
}
