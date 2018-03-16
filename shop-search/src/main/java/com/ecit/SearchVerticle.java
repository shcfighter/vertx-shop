package com.ecit;

import com.ecit.api.RestSearchRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.service.ICommodityService;
import com.ecit.service.impl.CommodityServiceImpl;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hubrick.vertx.elasticsearch.ElasticSearchServiceVerticle;
import com.hubrick.vertx.elasticsearch.TransportClientFactory;
import com.hubrick.vertx.elasticsearch.impl.DefaultElasticSearchService;
import com.hubrick.vertx.elasticsearch.impl.DefaultTransportClientFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.reactivex.core.Vertx;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.elasticsearch.common.settings.Settings;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class SearchVerticle extends BaseMicroserviceRxVerticle{

    private static final String SEARCH_SERVICE_NAME = "search_service_name";

    @Override
    public void start() throws Exception {
        super.start();
        ICommodityService userService = new CommodityServiceImpl(vertx, this.config());
        new ServiceBinder(vertx.getDelegate())
                .setAddress(ICommodityService.SEARCH_SERVICE_ADDRESS)
                .register(ICommodityService.class, userService);
        this.publishEventBusService(SEARCH_SERVICE_NAME, ICommodityService.SEARCH_SERVICE_ADDRESS, ICommodityService.class).subscribe();
        vertx.getDelegate().deployVerticle(new RestSearchRxVerticle(userService), new DeploymentOptions().setConfig(this.config()));
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
                            .put("api.name", "search")
                            .put("address", "eb.elasticsearch")
                            .put("transportAddresses", "[ { \"hostname\": \"localhost\", \"port\": 9300 }]")
                            .put("cluster_name", "elasticsearch")
                            .put("client_transport_sniff", false)
                    ));
        });
    }
}
