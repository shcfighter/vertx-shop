package com.ecit;

import com.ecit.api.RestCollectionRxVerticle;
import com.ecit.api.RestMessageRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.handler.ICollectionHandler;
import com.ecit.handler.IMessageHandler;
import com.ecit.handler.impl.CollectionHandler;
import com.ecit.handler.impl.MessageHandler;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by shwang on 2018/2/2.
 */
public class MessageVerticle extends BaseMicroserviceRxVerticle{

    private static final String SERVICE_MESSAGE_SERVICE_NAME = "service_message_service_api_name";

    @Override
    public void start() throws Exception {
        super.start();
        IMessageHandler messageHandler = new MessageHandler(vertx, this.config());
        ICollectionHandler collectionHandler = new CollectionHandler(vertx, this.config());
        new ServiceBinder(vertx.getDelegate()).setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).register(IMessageHandler.class, messageHandler);
        new ServiceBinder(vertx.getDelegate()).setAddress(ICollectionHandler.COLLECTION_SERVICE_ADDRESS).register(ICollectionHandler.class, collectionHandler);
        this.publishEventBusService(SERVICE_MESSAGE_SERVICE_NAME, IMessageHandler.MESSAGE_SERVICE_ADDRESS, IMessageHandler.class).subscribe();
        vertx.getDelegate().deployVerticle(RestMessageRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
        vertx.getDelegate().deployVerticle(RestCollectionRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
    }

    public static void main(String[] args) {
        Config cfg = new Config();
        GroupConfig group = new GroupConfig();
        group.setName("p-dev");
        group.setPassword("p-dev");
        cfg.setGroupConfig(group);
        // 申明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(MessageVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("message.api.name", "message")
                        .put("collection.api.name", "collection")
                        .put("mongodb", new JsonObject()
                                .put("host", "111.231.132.168")
                                .put("port", 27017)
                                .put("username", "shop_user")
                                .put("password", "h123456")
                                .put("db_name", "vertx_shop"))
                        .put("mail", new JsonObject()
                                .put("hostname", "smtp.mail.com")
                                .put("port", 25)
                                .put("username", "shc_fighter@mail.com")
                                .put("password", "1234567890a")
                                .put("starttls", "REQUIRED"))
                        .put("rabbitmq", new JsonObject()
                                .put("host", "111.231.132.168")
                                .put("port", 5672)
                                .put("username", "guest")
                                .put("password", "guest")
                                .put("virtualHost", "/"))
                )));
    }
}
