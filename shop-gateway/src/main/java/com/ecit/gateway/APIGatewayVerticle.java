package com.ecit.gateway;

import com.ecit.common.RestAPIVerticle;
import com.ecit.constants.UserSql;
import com.ecit.enmu.UserStatus;
import com.ecit.gateway.auth.ShopAuth;
import com.ecit.gateway.auth.impl.ShopUser;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.util.CollectionUtil;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A verticle for global API gateway.
 * This API gateway uses HTTP-HTTP pattern. It's also responsible for
 * load balance and failure handling.
 *
 * @author Eric Zhao
 */
public class APIGatewayVerticle extends RestAPIVerticle {

    private static final int DEFAULT_PORT = 8787;

    private static final Logger LOGGER = LogManager.getLogger(APIGatewayVerticle.class);

    private ShopAuth shopAuthProvider;
    private JDBCClient jdbcClient;

    private AuthHandler basicAuthHandler;

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
                        .put("url", "jdbc:postgresql://localhost:5432/vertx_shop")
                        .put("driver_class", "org.postgresql.Driver")
                        .put("max_pool_size", 50)
                        .put("user", "postgres")
                        .put("password", "123456"))));
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start();

        jdbcClient = JDBCClient.createNonShared(vertx, this.config());
        shopAuthProvider = ShopAuth.create(vertx, jdbcClient);
        basicAuthHandler = BasicAuthHandler.create(shopAuthProvider);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("api.gateway.http.address", "192.168.197.227");
        int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);

        Router router = Router.router(vertx);
        // cookie and session handler
        this.enableLocalSession(router);
        this.enableCorsSupport(router);

        // body handler
        router.route().handler(BodyHandler.create());
        router.route().handler(UserSessionHandler.create(shopAuthProvider));

        // version handler
        router.get("/api/v").handler(this::apiVersion);
        router.route("/api/*").handler(this::formatContentTypeHandler);
        router.post("/api/user/login").handler(this::loginEntryHandler);
        router.get("/uaa").handler(this::authUaaHandler);
        router.post("/logout").handler(this::logoutHandler);
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);

        // enable HTTPS
        HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath("server.jks").setPassword("123456"));

        // create http server
        vertx.createHttpServer(/*httpServerOptions*/)
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(ar.cause());
                    }
                });
    }

    private void dispatchRequests(RoutingContext context) {
        int initialOffset = 5; // length of `/api/`
        String path = context.request().uri();
        if (path.length() <= initialOffset) {
            notFound(context);
            return;
        }
        String prefix = (path.substring(initialOffset).split("/"))[0];
        // generate new relative path
        String newPath = path.substring(initialOffset + prefix.length());

        // run with circuit breaker in order to deal with failure
        circuitBreaker.execute(future -> {
            getAllEndpoints(prefix).setHandler(ar -> {
                if (ar.succeeded()) {
                    List<Record> records = ar.result();
                    //负载均衡策略：随机
                    Optional<Record> client = Optional.ofNullable(CollectionUtil.isEmpty(records) ? null : records.get(new Random().nextInt(records.size())));
                    if (client.isPresent()) {
                        doDispatch(context, newPath, discovery.getReference(client.get()).getAs(WebClient.class), future);
                    } else {
                        notFound(context);
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });

            /*HttpEndpoint.getWebClient(discovery, new JsonObject().put("api.name", prefix), handler -> {
                if(handler.succeeded()){
                    doDispatch(context, newPath, handler.result(), future);
                } else {
                    LOGGER.error("获取webclient【{}】失败！", prefix);
                    future.fail(handler.cause());
                }
            });*/
        }).setHandler(ar -> {
            if (ar.failed()) {
                badGateway(ar.cause(), context);
            }
        });
    }

    /**
     * Dispatch the request to the downstream REST layers.
     *
     * @param context routing context instance
     * @param path    relative path
     * @param client  relevant HTTP client
     */
    private void doDispatch(RoutingContext context, String path, WebClient client, Future<Object> cbFuture) {
        final HttpRequest<Buffer> request = client.request(context.request().method(), path);
        Optional.ofNullable(context.getBodyAsJson()).orElse(new JsonObject()).getMap().forEach((k, v) -> request.addQueryParam(k, String.valueOf(v)));
        if (context.user() != null) {
            request.putHeader("user-principal", context.user().principal().encode());
        }
        request.send(handler -> {
            Buffer bodyBuffer = Buffer.buffer();
            if(handler.succeeded()){
                System.out.println(handler.result().body().toString());
                bodyBuffer.appendBuffer(handler.result().body());

            } else {
                LOGGER.error("调用http接口错误！", handler.cause());
                bodyBuffer.appendString("{\"error\": \"远程接口调用失败！\"}");
            }
            context.response().setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(bodyBuffer);
            cbFuture.complete();
            ServiceDiscovery.releaseServiceObject(discovery, client);
        });
    }

    /**
     * Get all REST endpoints from the service discovery infrastructure.
     *
     * @return async result
     */
    private Future<List<Record>> getAllEndpoints(String prefix) {
        Future<List<Record>> future = Future.future();
        discovery.getRecords(record -> record.getType().equals(HttpEndpoint.TYPE)
                        && StringUtils.equals(record.getMetadata().getString("api.name"), prefix),
                future.completer());
        return future;
    }

    private void apiVersion(RoutingContext context) {
        context.response()
                .end(new JsonObject().put("version", "v1").encodePrettily());
    }

    /**
     * 校验请求格式
     * @param context
     */
    private void formatContentTypeHandler(RoutingContext context) {
        if(!StringUtils.startsWithIgnoreCase(context.request().getHeader("Content-Type"), "application/json")){
            LOGGER.error("请求方式不正确【{}】", context.request().getHeader("Content-Type"));
            badRequest(context, new Throwable("请求方式【Content-Type】错误"));
            return;
        }
        context.next();
    }

    private void authUaaHandler(RoutingContext context) {
        if (context.user() != null) {
            this.returnWithSuccessMessage(context, context.user().principal().encodePrettily());
        } else {
            context.fail(401);
        }
    }

    /**
     * 登录
     * @param context
     */
    private void loginEntryHandler(RoutingContext context) {
        JsonObject params = context.getBodyAsJson();
        final String loginName = params.getString("loginName");
        LOGGER.info("用户【{}】登录系统", loginName);
        shopAuthProvider.setAuthenticationQuery(UserSql.LOGIN_SQL);
        shopAuthProvider.authenticate(new JsonObject().put("status", UserStatus.ACTIVATION.getStatus())
                .put("loginName", loginName).put("password", params.getString("pwd")), handler -> {
            if (handler.succeeded()) {
                ShopUser userSession = (ShopUser) handler.result();
                context.setUser(userSession);
                if(Objects.isNull(userSession)){
                    LOGGER.info("用户【{}】登录，用户不存在", loginName);
                    this.returnWithFailureMessage(context, "用户名或密码错误");
                } else {
                    LOGGER.info("用户【{}】登录成功", userSession.principal());
                    this.returnWithSuccessMessage(context, "登录成功");
                }
            } else {
                LOGGER.error("调用远程登录方法错误！", handler.cause());
                this.badGateway(handler.cause(), context);
            }
        });
    }

    private void logoutHandler(RoutingContext context) {
        context.clearUser();
        context.session().destroy();
        context.response().setStatusCode(204).end();
    }
}
