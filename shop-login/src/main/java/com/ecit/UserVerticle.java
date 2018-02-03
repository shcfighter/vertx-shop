package com.ecit;

import com.ecit.api.RestUserVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.service.IUserService;
import com.ecit.service.impl.UserServiceImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class UserVerticle extends BaseMicroserviceRxVerticle{

    @Override
    public void start() throws Exception {
        super.start();
        IUserService userService = new UserServiceImpl(vertx, this.config());
        new ServiceBinder(vertx.getDelegate())
                .setAddress(IUserService.USER_SERVICE_ADDRESS)
                .register(IUserService.class, userService);
        vertx.deployVerticle("com.ecit.api.RestUserVerticle");
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
        Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(UserVerticle.class.getName()));
    }
}
