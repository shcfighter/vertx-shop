package com.ecit;

import com.ecit.api.RestMessageRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.service.IMessageService;
import com.ecit.service.impl.MessageServiceImpl;
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
 * Created by za-wangshenhua on 2018/2/2.
 */
public class MessageVerticle extends BaseMicroserviceRxVerticle{

    private static final String SERVICE_MESSAGE_SERVICE_NAME = "service_message_service_api_name";

    @Override
    public void start() throws Exception {
        super.start();
        IMessageService messageService = new MessageServiceImpl(vertx, this.config());
        new ServiceBinder(vertx.getDelegate())
                .setAddress(IMessageService.MESSAGE_SERVICE_ADDRESS)
                .register(IMessageService.class, messageService);
        this.publishEventBusService(SERVICE_MESSAGE_SERVICE_NAME, IMessageService.MESSAGE_SERVICE_ADDRESS, IMessageService.class).subscribe();
        vertx.getDelegate().deployVerticle(new RestMessageRxVerticle(messageService), new DeploymentOptions().setConfig(this.config()));
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
                        .put("api.name", "message")
                        .put("host", "192.168.101.154")
                        .put("port", 27017)
                        .put("username", "shop_user")
                        .put("password", "123456")
                        .put("db_name", "shop_message")
                )));
    }
}
