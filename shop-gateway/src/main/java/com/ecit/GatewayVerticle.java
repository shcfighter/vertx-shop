package com.ecit;

import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.gateway.APIGatewayVerticle;
import com.hazelcast.config.Config;
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
        // 申明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        io.vertx.reactivex.core.Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(APIGatewayVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("postgresql", new JsonObject()
                                .put("host", "127.0.0.1")
                                .put("port", 5432)
                                .put("maxPoolSize", 50)
                                .put("username", "postgres")
                                .put("password", "123456")
                                .put("database", "vertx_shop")
                                .put("charset", "UTF-8")
                                .put("queryTimeout", 10000))
                        .put("redis", new JsonObject()
                                .put("host", "127.0.0.1")
                                .put("port", 6379)
                                .put("auth", "123456"))
                )));
    }
}
