package com.ecit;

import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.gateway.APIGatewayVerticle;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by shwang on 2018/2/2.
 */
public class GatewayVerticle extends BaseMicroserviceRxVerticle{

    @Override
    public void start() throws Exception {
        super.start();
        vertx.getDelegate().deployVerticle(APIGatewayVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
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
        io.vertx.reactivex.core.Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(APIGatewayVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("url", "jdbc:postgresql://111.231.132.168:5432/vertx_shop")
                        .put("driver_class", "org.postgresql.Driver")
                        .put("max_pool_size", 50)
                        .put("user", "postgres")
                        .put("password", "h123456"))));
    }
}
