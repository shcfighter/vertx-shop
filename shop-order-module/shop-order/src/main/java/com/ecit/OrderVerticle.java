package com.ecit;

import com.ecit.api.RestCartRxVerticle;
import com.ecit.api.RestOrderRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.handler.ICartHandler;
import com.ecit.handler.IOrderHandler;
import com.ecit.handler.impl.CartHandler;
import com.ecit.handler.impl.OrderHandler;
import com.hazelcast.config.Config;
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
public class OrderVerticle extends BaseMicroserviceRxVerticle{

    private static final String ORDER_SERVICE_NAME = "order_service_name";
    private static final String CART_SERVICE_NAME = "cart_service_name";

    @Override
    public void start() throws Exception {
        super.start();
        IOrderHandler orderService = new OrderHandler(vertx, this.config());
        ICartHandler cartService = new CartHandler(vertx, this.config());
        new ServiceBinder(vertx.getDelegate()).setAddress(IOrderHandler.ORDER_SERVICE_ADDRESS).register(IOrderHandler.class, orderService);
        new ServiceBinder(vertx.getDelegate()).setAddress(ICartHandler.CART_SERVICE_ADDRESS).register(ICartHandler.class, cartService);
        this.publishEventBusService(ORDER_SERVICE_NAME, IOrderHandler.ORDER_SERVICE_ADDRESS, IOrderHandler.class).subscribe();
        this.publishEventBusService(CART_SERVICE_NAME, ICartHandler.CART_SERVICE_ADDRESS, ICartHandler.class).subscribe();
        vertx.getDelegate().deployVerticle(RestOrderRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
        vertx.getDelegate().deployVerticle(RestCartRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
    }

    public static void main(String[] args) {
        Config cfg = new Config();

        // 申明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.rxClusteredVertx(options).subscribe(v -> {
            v.deployVerticle(OrderVerticle.class.getName(),
                    new DeploymentOptions().setConfig(new JsonObject()
                            .put("order.api.name", "order")
                            .put("cart.api.name", "cart")
                            .put("postgresql", new JsonObject()
                                    .put("host", "111.231.132.168")
                                    .put("port", 5432)
                                    .put("maxPoolSize", 50)
                                    .put("username", "postgres")
                                    .put("password", "h123456")
                                    .put("database", "vertx_shop")
                                    .put("charset", "UTF-8")
                                    .put("queryTimeout", 10000))
                            .put("mongodb", new JsonObject()
                                    .put("host", "111.231.132.168")
                                    .put("port", 27017)
                                    .put("username", "shop_user")
                                    .put("password", "h123456")
                                    .put("db_name", "vertx_shop"))
                            .put("redis", new JsonObject()
                                    .put("host", "111.231.132.168")
                                    .put("port", 6379)
                                    .put("auth", "h123456"))
                    ));
        });
    }
}
