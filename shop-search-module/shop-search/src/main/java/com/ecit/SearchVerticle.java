package com.ecit;

import com.ecit.api.RestSearchRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.handler.ICommodityHandler;
import com.ecit.handler.IPreferencesHandler;
import com.ecit.handler.impl.CommodityHandler;
import com.ecit.handler.impl.PreferencesHandler;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hubrick.vertx.elasticsearch.ElasticSearchServiceVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * Created by shwang on 2018/2/2.
 */
public class SearchVerticle extends BaseMicroserviceRxVerticle{

    private static final String SEARCH_SERVICE_NAME = "search_service_name";

    @Override
    public void start() throws Exception {
        super.start();
        ICommodityHandler commodityHandler = new CommodityHandler(vertx, this.config());
        IPreferencesHandler preferencesHandler = new PreferencesHandler(vertx, this.config());
        new ServiceBinder(vertx.getDelegate()).setAddress(ICommodityHandler.SEARCH_SERVICE_ADDRESS).register(ICommodityHandler.class, commodityHandler);
        new ServiceBinder(vertx.getDelegate()).setAddress(IPreferencesHandler.SEARCH_SERVICE_PREFERENCES).register(IPreferencesHandler.class, preferencesHandler);
        this.publishEventBusService(SEARCH_SERVICE_NAME, ICommodityHandler.SEARCH_SERVICE_ADDRESS, ICommodityHandler.class).subscribe();
        vertx.getDelegate().deployVerticle(RestSearchRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances")));
        vertx.getDelegate().deployVerticle(ElasticSearchServiceVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances")));
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
            v.deployVerticle(SearchVerticle.class.getName(),
                    new DeploymentOptions().setConfig(new JsonObject()
                            .put("search.api.name", "search")
                            .put("address", "eb.elasticsearch")
                            .put("transportAddresses", new JsonArray().add(new JsonObject()
                                    .put("hostname", "111.231.132.168")
                                    .put("port", 9300)))
                            .put("cluster_name", "vertx_shop")
                            .put("client_transport_sniff", false)
                            .put("host", "111.231.132.168")
                            .put("port", 5432)
                            .put("maxPoolSize", 50)
                            .put("username", "postgres")
                            .put("password", "h123456")
                            .put("database", "vertx_shop")
                            .put("charset", "UTF-8")
                            .put("queryTimeout", 10000)
                            .put("mongodb", new JsonObject()
                                    .put("host", "111.231.132.168")
                                    .put("port", 27017)
                                    .put("username", "shop_user")
                                    .put("password", "h123456")
                                    .put("db_name", "vertx_shop"))
                            .put("rabbitmq", new JsonObject()
                                    .put("host", "localhost")
                                    .put("port", 5672)
                                    .put("username", "guest")
                                    .put("password", "guest")
                                    .put("virtualHost", "/"))
                    ));
        });
    }
}
