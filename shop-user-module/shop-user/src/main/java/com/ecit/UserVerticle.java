package com.ecit;

import com.ecit.api.RestAccountRxVerticle;
import com.ecit.api.RestUserRxVerticle;
import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.handler.IAccountHandler;
import com.ecit.handler.IAddressHandler;
import com.ecit.handler.ICertifiedHandler;
import com.ecit.handler.IUserHandler;
import com.ecit.handler.impl.AccountHandler;
import com.ecit.handler.impl.AddressHandler;
import com.ecit.handler.impl.CertifiedHandler;
import com.ecit.handler.impl.UserHandler;
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
public class UserVerticle extends BaseMicroserviceRxVerticle{

    private static final String USER_SERVICE_NAME = "user_service_name";

    @Override
    public void start() throws Exception {
        super.start();
        IUserHandler userHandler = new UserHandler(vertx, this.config());
        ICertifiedHandler certifiedHandler = new CertifiedHandler(vertx, this.config());
        IAddressHandler addressHandler = new AddressHandler(vertx, this.config());
        IAccountHandler accountHandler = new AccountHandler(vertx, this.config());
        new ServiceBinder(vertx.getDelegate()).setAddress(IUserHandler.USER_SERVICE_ADDRESS).register(IUserHandler.class, userHandler);
        new ServiceBinder(vertx.getDelegate()).setAddress(ICertifiedHandler.CERTIFIED_SERVICE_ADDRESS).register(ICertifiedHandler.class, certifiedHandler);
        new ServiceBinder(vertx.getDelegate()).setAddress(IAddressHandler.ADDRESS_SERVICE_ADDRESS).register(IAddressHandler.class, addressHandler);
        new ServiceBinder(vertx.getDelegate()).setAddress(IAccountHandler.ACCOUNT_SERVICE_ADDRESS).register(IAccountHandler.class, accountHandler);
        //this.publishEventBusService(USER_SERVICE_NAME, IUserHandler.USER_SERVICE_ADDRESS, IUserHandler.class).subscribe();
        vertx.getDelegate().deployVerticle(RestUserRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
        vertx.getDelegate().deployVerticle(RestAccountRxVerticle.class, new DeploymentOptions().setConfig(this.config()).setInstances(this.config().getInteger("instances", 1)));
    }

    public static void main(String[] args) {
        Config cfg = new Config();
        // 申明集群管理器
        ClusterManager mgr = new HazelcastClusterManager(cfg);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);
        Vertx.rxClusteredVertx(options).subscribe(v -> v.deployVerticle(UserVerticle.class.getName(),
                new DeploymentOptions().setConfig(new JsonObject()
                        .put("user.api.name", "user")
                        .put("account.api.name", "account")
                        .put("postgresql", new JsonObject()
                                .put("host", "127.0.0.1")
                                .put("port", 5432)
                                .put("maxPoolSize", 50)
                                .put("username", "postgres")
                                .put("password", "h123456")
                                .put("database", "vertx_shop")
                                .put("charset", "UTF-8")
                                .put("queryTimeout", 10000))
                        .put("rabbitmq", new JsonObject()
                                .put("host", "127.0.0.1")
                                .put("port", 5672)
                                .put("username", "guest")
                                .put("password", "guest")
                                .put("virtualHost", "/"))
                        .put("redis", new JsonObject()
                                .put("host", "127.0.0.1")
                                .put("port", 6379)
                                .put("auth", "h123456"))
                )));
    }
}
