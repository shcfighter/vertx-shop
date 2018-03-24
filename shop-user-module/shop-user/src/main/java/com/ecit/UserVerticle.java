package com.ecit;

import com.ecit.api.RestUserRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.service.IUserService;
import com.ecit.service.impl.UserServiceImpl;
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
public class UserVerticle extends BaseMicroserviceRxVerticle{

    private static final String USER_SERVICE_NAME = "user_service_name";

    @Override
    public void start() throws Exception {
        super.start();
        IUserService userService = new UserServiceImpl(vertx, this.config());
        new ServiceBinder(vertx.getDelegate())
                .setAddress(IUserService.USER_SERVICE_ADDRESS)
                .register(IUserService.class, userService);
        this.publishEventBusService(USER_SERVICE_NAME, IUserService.USER_SERVICE_ADDRESS, IUserService.class).subscribe();
        vertx.getDelegate().deployVerticle(new RestUserRxVerticle(userService), new DeploymentOptions().setConfig(this.config()));
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
        Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(UserVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("api.name", "user")
                        .put("host", "111.231.132.168")
                        .put("port", 5432)
                        .put("maxPoolSize", 50)
                        .put("username", "postgres")
                        .put("password", "h123456")
                        .put("database", "vertx_shop")
                        .put("charset", "UTF-8")
                        .put("queryTimeout", 10000)
                )));
    }
}
