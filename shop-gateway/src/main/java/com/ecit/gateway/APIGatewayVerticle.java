package com.ecit.gateway;

import com.ecit.common.RestAPIVerticle;
import com.ecit.constants.UserSql;
import com.ecit.enmu.UserLock;
import com.ecit.enmu.UserStatus;
import com.ecit.service.IUserService;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jdbc.JDBCAuth;
import io.vertx.ext.auth.jdbc.impl.JDBCUser;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;

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

    private JDBCAuth jdbcAuthProvider;
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
        jdbcAuthProvider = JDBCAuth.create(vertx, jdbcClient);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("api.gateway.http.address", "localhost");
        int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);

        Router router = Router.router(vertx);
        // cookie and session handler
        this.enableLocalSession(router);
        this.enableCorsSupport(router);

        // body handler
        router.route().handler(BodyHandler.create());
        router.route().handler(UserSessionHandler.create(jdbcAuthProvider));

        // version handler
        router.get("/api/v").handler(this::apiVersion);
        router.route("/api/*").handler(this::formatContentTypeHandler);
        router.post("/api/user/register").handler(this::registerHandler);
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
            HttpEndpoint.getWebClient(discovery, new JsonObject().put("api.name", prefix), handler -> {
                if(handler.succeeded()){
                    doDispatch(context, newPath, handler.result(), future);
                } else {
                    LOGGER.error("获取webclient【{}】失败！", prefix);
                    future.fail(handler.cause());
                }
            });
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
        });
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

    /**
     * 注册
     * @param context 上下文
     */
    private void registerHandler(RoutingContext context) {
        final String salt = jdbcAuthProvider.generateSalt();
        JsonObject params = context.getBodyAsJson();
        context.setBody(params
                .put("pwd", jdbcAuthProvider.computeHash(params.getString("pwd"), salt))
                .put("salt", salt).toBuffer());
        this.dispatchRequests(context);
    }

    private void authUaaHandler(RoutingContext context) {
        if (context.user() != null) {
            JsonObject principal = context.user().principal();
            String username = null;  // TODO: Only for demo. Complete this in next version.
            //String username = KeycloakHelper.preferredUsername(principal);
            if (username == null) {
                context.response()
                        .putHeader("content-type", "application/json")
                        .end(/*new Account().setId("TEST666").setUsername("Eric").toString()*/); // TODO: no username should be an error
            } else {
                /*Future<AccountService> future = Future.future();
                EventBusService.getProxy(discovery, AccountService.class, future.completer());
                future.compose(accountService -> {
                    Future<Account> accountFuture = Future.future();
                    accountService.retrieveByUsername(username, accountFuture.completer());
                    return accountFuture.map(a -> {
                        io.vertx.servicediscovery.ServiceDiscovery.releaseServiceObject(discovery, accountService);
                        return a;
                    });
                }).setHandler(resultHandlerNonEmpty(context));*/ // if user does not exist, should return 404
            }
        } else {
            context.fail(401);
        }
    }

    /**
     * 登录
     * @param context
     */
    private void loginEntryHandler(RoutingContext context) {
        /*JsonObject params = context.getBodyAsJson();
        final String loginName = params.getString("loginName");
        LOGGER.info("用户【】登录系统", loginName);
        jdbcAuthProvider.setAuthenticationQuery(UserSql.LOGIN_SQL);
        jdbcAuthProvider.authenticate(new JsonObject().put("status", UserStatus.ACTIVATION.getStatus())
                .put("is_lock", UserLock.PUBLISH.getLock()).put("mobile", loginName)
                .put("email", loginName).put("login_name", loginName), handler -> {
            if (handler.succeeded()) {
                JDBCUser userSession = (JDBCUser) handler.result();
                if(Objects.isNull(userSession)){
                    LOGGER.info("用户【{}】登录，用户不存在", loginName);
                    this.returnWithMessage(context, "用户名或密码错误");
                } else {
                    LOGGER.info("用户【{}】登录成功", userSession.principal());
                    this.returnWithMessage(context, "登录成功");
                }
            } else {
                LOGGER.error("调用远程登录方法错误！", handler.cause());
                this.badGateway(handler.cause(), context);
            }
        });*/
        EventBusService.getProxy(discovery, IUserService.class, resultHandler -> {
            if (resultHandler.succeeded()) {
                final IUserService userService = resultHandler.result();
                JsonObject params = context.getBodyAsJson();
                final String loginName = params.getString("loginName");
                LOGGER.info("用户【】登录系统", loginName);
                userService.login(loginName, params.getString("pwd"), handler -> {
                    if (handler.succeeded()) {
                        JsonObject userObject = handler.result();
                        if(Objects.isNull(userObject) || userObject.isEmpty()){
                            LOGGER.info("用户【{}】登录，用户不存在", loginName);
                            this.returnWithMessage(context, "用户名或密码错误");
                        } else {
                            if(StringUtils.equals(jdbcAuthProvider.computeHash(params.getString("pwd"), userObject.getString("salt")), userObject.getString("pwd"))){
                                LOGGER.info("用户id【】登录成功", userObject.getString("userId"));
                                //JDBCUser user = new JDBCUser("",jdbcAuthProvider, "");
                                this.returnWithMessage(context, "登录成功");
                            } else {
                                LOGGER.info("用户id【】登录密码错误", userObject.getString("userId"));
                                this.returnWithMessage(context, "用户名或密码错误");
                            }

                        }
                    } else {
                        LOGGER.error("调用远程登录方法错误！", handler.cause());
                        this.badGateway(resultHandler.cause(), context);
                    }
                });
                ServiceDiscovery.releaseServiceObject(discovery, userService);
            } else {
                LOGGER.error("远程调用登录接口失败！", resultHandler.cause());
                this.badGateway(resultHandler.cause(), context);
            }
        });
    }

    private void logoutHandler(RoutingContext context) {
        context.clearUser();
        context.session().destroy();
        context.response().setStatusCode(204).end();
    }
}
