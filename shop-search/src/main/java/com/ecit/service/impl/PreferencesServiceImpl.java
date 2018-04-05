package com.ecit.service.impl;

import com.ecit.SearchType;
import com.ecit.service.IPreferencesService;
import io.reactivex.Completable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.rabbitmq.RabbitMQClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by za-wangshenhua on 2018/3/22.
 */
public class PreferencesServiceImpl implements IPreferencesService{

    final MongoClient mongoClient;
    final RabbitMQClient rabbitMQClient;
    final Vertx vertx;
    private final static Logger LOGGER = LogManager.getLogger(PreferencesServiceImpl.class);
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
    private static final String QUEUES = "vertx.shop.queues";


    public PreferencesServiceImpl(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
        rabbitMQClient = RabbitMQClient.create(vertx, new RabbitMQOptions(config.getJsonObject("rabbitmq")));
        /**
         * 创建rabbitmq连接
         */
        rabbitMQClient.start(startMQ -> {
            if (startMQ.succeeded()) {
                LOGGER.info("rabbit mq start success !");
                this.savePreferences().subscribe();
            } else {
                LOGGER.error("rabbitmq start failed !");
            }
        });
    }

    /**
     * 保存查询关键字
     * @return
     */
    @Override
    public Completable savePreferences() {
        vertx.eventBus().consumer("my.address", msg -> {
            JsonObject json = (JsonObject) msg.body();
            JsonObject perferences = new JsonObject(json.getString("body"));
            System.out.println("Got message: " + perferences);
            mongoClient.rxInsert(PREFERENCES_COLLECTION, new JsonObject(json.getString("body")))
                    .subscribe();
        });

        // Setup the link between rabbitmq consumer and event bus address
        return rabbitMQClient.rxBasicConsume(QUEUES, "my.address");
    }

    /**
     * 历史查询关键字
     * @param cookies
     * @param handler
     * @return
     */
    @Override
    public IPreferencesService findPreferences(String cookies, Handler<AsyncResult<List<String>>> handler) {
        Future<List<String>> future = Future.future();
        mongoClient.rxFindWithOptions(PREFERENCES_COLLECTION, new JsonObject().put("cookies", cookies),
                new FindOptions().setLimit(3).setSort(new JsonObject().put("create_time", -1)))
                .subscribe(res -> future.complete(res.stream().map(jsonObject -> jsonObject.getString("keyword")).collect(Collectors.toList())), future::fail);
        future.setHandler(handler);
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
    public IPreferencesService sendMqPreferences(String cookies, String keyword, SearchType searchType, Handler<AsyncResult<String>> handler) {
        Future<String> future = Future.future();
        if(StringUtils.isEmpty(cookies)){
            future = Future.failedFuture(new NullPointerException("cookies 为null"));
            future.setHandler(handler);
            return this;
        }
        JsonObject message = new JsonObject()
                .put("body", new JsonObject().put("cookies", cookies)
                                            .put("keyword", keyword)
                                            .put("search_type", searchType.name())
                                            .put("create_time", new Date().getTime()).encodePrettily());
        rabbitMQClient.rxBasicPublish(EXCHANGE, ROUTINGKEY, message).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
