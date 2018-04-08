package com.ecit.gateway;

import com.ecit.common.RestAPIVerticle;
import com.ecit.common.result.ResultItems;
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
import io.vertx.core.http.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
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

/**
 * A verticle for global API gateway.
 * This API gateway uses HTTP-HTTP pattern. It's also responsible for
 * load balance and failure handling.
 *
 * @author Eric Zhao
 */
public class APIGatewayVerticle extends RestAPIVerticle {

    private static final Logger LOGGER = LogManager.getLogger(APIGatewayVerticle.class);
    private static final int DEFAULT_PORT = 8787;

    private ShopAuth shopAuthProvider;
    private JDBCClient jdbcClient;

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

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start();

        jdbcClient = JDBCClient.createNonShared(vertx, this.config());
        shopAuthProvider = ShopAuth.create(vertx, jdbcClient);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("api.gateway.http.address", "localhost");
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
        //全局异常处理
        this.globalVerticle(router);

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
                        doDispatch(context, newPath, discovery.getReference(client.get()).getAs(HttpClient.class), future);
                    } else {
                        notFound(context);
                        future.complete();
                    }
                } else {
                    future.fail(ar.cause());
                }
            });
        }).setHandler(ar -> {
            if (ar.failed()) {
                LOGGER.error("gateway调用失败！", ar.cause());
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
    private void doDispatch(RoutingContext context, String path, HttpClient client, Future<Object> cbFuture) {
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response()
                                    .setStatusCode(response.statusCode());
                            response.headers().forEach(header -> {
                                toRsp.putHeader(header.getKey(), header.getValue());
                            });
                            // send response
                            toRsp.end(body);
                            cbFuture.complete();
                        }
                        ServiceDiscovery.releaseServiceObject(discovery, client);
                    });
                });
        // set headers
        context.request().headers().forEach(header -> {
            toReq.putHeader(header.getKey(), header.getValue());
        });
        context.cookies().forEach(cookie -> {
            toReq.putHeader(cookie.getName(), cookie.getValue());
        });
        if (context.user() != null) {
            toReq.putHeader("user-principal", context.user().principal().encode());
        }
        // send request
        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }

        /*final HttpRequest<Buffer> request = client.request(context.request().method(), path);
        if(context.request().method().compareTo(HttpMethod.POST) == 0
                || context.request().method().compareTo(HttpMethod.PUT) == 0){
            Optional.ofNullable(context.getBodyAsJson()).orElse(new JsonObject()).getMap().forEach((k, v) -> request.addQueryParam(k, String.valueOf(v)));
        }
        context.cookies().forEach(cookie -> {
            request.putHeader(cookie.getName(), cookie.getValue());
        });
        if (context.user() != null) {
            request.putHeader("user-principal", context.user().principal().encode());
        }
        request.send(handler -> {
            Buffer bodyBuffer = Buffer.buffer();
            if(handler.succeeded()){
                bodyBuffer.appendBuffer(handler.result().body());

            } else {
                LOGGER.error("调用http接口错误！", handler.cause());
                bodyBuffer.appendString(ResultItems.getEncodePrettily(new ResultItems(-1, "远程接口调用失败！")));
            }
            context.response().setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(bodyBuffer);
            cbFuture.complete();
            ServiceDiscovery.releaseServiceObject(discovery, client);
        });*/
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
        if(context.request().method().compareTo(HttpMethod.POST) == 0
                && context.request().method().compareTo(HttpMethod.PUT) == 0
                && !StringUtils.startsWithIgnoreCase(context.request().getHeader("Content-Type"), "application/json")){
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
     * @param context 上下文
     */
    private void loginEntryHandler(RoutingContext context) {
        final JsonObject params = context.getBodyAsJson();
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
                    this.returnWithSuccessMessage(context, "登录成功", userSession.principal());
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
