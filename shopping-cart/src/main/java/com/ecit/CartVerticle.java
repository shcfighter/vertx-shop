package com.ecit;

import com.ecit.api.RestCartRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.service.ICartService;
import com.ecit.service.impl.CartServiceImpl;
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
 * 购物车
 * Created by za-wangshenhua on 2018/2/2.
 */
public class CartVerticle extends BaseMicroserviceRxVerticle{

    private static final String CART_SERVICE_NAME = "cart_service_name";

    @Override
    public void start() throws Exception {
        super.start();
        ICartService cartService = new CartServiceImpl(vertx, this.config());
        new ServiceBinder(vertx.getDelegate())
                .setAddress(ICartService.CART_SERVICE_ADDRESS)
                .register(ICartService.class, cartService);
        this.publishEventBusService(CART_SERVICE_NAME, ICartService.CART_SERVICE_ADDRESS, ICartService.class).subscribe();
        vertx.getDelegate().deployVerticle(new RestCartRxVerticle(cartService), new DeploymentOptions().setConfig(this.config()));
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
        Vertx.rxClusteredVertx(options).subscribe(v -> {
            v.deployVerticle(CartVerticle.class.getName(),
                    new DeploymentOptions().setConfig(new JsonObject()
                            .put("api.name", "cart")
                            .put("host", "111.231.132.168")
                            .put("port", 27017)
                            .put("username", "shop_user")
                            .put("password", "h123456")
                            .put("db_name", "vertx_shop")
                    ));
        });
    }
}
