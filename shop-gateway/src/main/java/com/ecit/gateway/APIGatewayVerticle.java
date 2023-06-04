package com.ecit.gateway;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.IpUtils;
import com.ecit.enmu.UserStatus;
import com.ecit.gateway.auth.ShopAuthHandler;
import com.hazelcast.util.CollectionUtil;
import com.hazelcast.util.UuidUtil;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.reactivex.core.file.FileSystem;
import io.vertx.reactivex.core.http.HttpClient;
import io.vertx.reactivex.core.http.HttpClientRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.FileUpload;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * A verticle for global API gateway.
 * This API gateway uses HTTP-HTTP pattern. It's also responsible for
 * load balance and failure handling.
 *
 * @author shwang
 */
public class APIGatewayVerticle extends RestAPIRxVerticle {

    private static final Logger LOGGER = LogManager.getLogger(APIGatewayVerticle.class);
    private static final int DEFAULT_PORT = 8787;
    private ShopAuthHandler authHandler;

    @Override
    public void start(Future<Void> future) throws Exception {
        super.start();

        authHandler = ShopAuthHandler.create(vertx, this.config());

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("api.gateway.http.address", "localhost");
        int port = config().getInteger("api.gateway.http.port", DEFAULT_PORT);

        Router router = Router.router(vertx);
        // cookie and session handler
        this.enableLocalSession(router, "shop_session");
        //this.enableCorsSupport(router);
        // body handler
        router.route().handler(BodyHandler.create());

        // version handler
        router.get("/api/v").handler(this::apiVersion);
        router.post("/api/uploadImageAvatar").handler(this::uploadImageAvatarHandler);
        router.get("/api/uaa").handler(this::authUaaHandler);
        router.route("/api/*").handler(this::formatContentTypeHandler);
        router.post("/api/user/login").handler(this::loginEntryHandler);
        router.get("/api/user/logout").handler(this::logoutHandler);
        // api dispatcher
        router.route("/api/*").handler(this::dispatchRequests);

        //全局异常处理
        this.globalVerticle(router);

        // enable HTTPS
        /*HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setSsl(true)
                .setKeyStoreOptions(new JksOptions().setPath("server.jks").setPassword("123456"));*/

        // create http server
        vertx.createHttpServer(/*httpServerOptions*/)
                .requestHandler(router::accept)
                .listen(port, host, ar -> {
                    if (ar.succeeded()) {
                        future.complete();
                        LOGGER.info("shop-gateway server started!");
                    } else {
                        future.fail(ar.cause());
                        LOGGER.info("shop-gateway server fail!", ar.cause());
                    }
                });
    }

    private void dispatchRequests(RoutingContext context) {
        final int initialOffset = 5; // length of `/api/`
        String path = context.request().uri();
        LOGGER.info("接口【{}】调用，远程ip【{}】", path, IpUtils.getIpAddr(context.request()));
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
    private void doDispatch(RoutingContext context, String path, HttpClient client, io.vertx.reactivex.core.Future<Object> cbFuture) {
        HttpClientRequest toReq = client
                .request(context.request().method(), path, response -> {
                    response.bodyHandler(body -> {
                        if (response.statusCode() >= 500) { // api endpoint server error, circuit breaker should fail
                            cbFuture.fail(response.statusCode() + ": " + body.toString());
                        } else {
                            HttpServerResponse toRsp = context.response()
                                    .setStatusCode(response.statusCode());
                            response.getDelegate().headers().forEach(header -> {
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
        context.request().getDelegate().headers().forEach(header -> {
            toReq.putHeader(header.getKey(), header.getValue());
        });
        context.cookies().forEach(cookie -> {
            toReq.putHeader(cookie.getName(), cookie.getValue());
        });
        //set ip
        toReq.putHeader("ip", IpUtils.getIpAddr(context.request()));
        // send request
        if (context.getBody() == null) {
            toReq.end();
        } else {
            toReq.end(context.getBody());
        }
    }

    /**
     * Get all REST endpoints from the handler discovery infrastructure.
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
        if((context.request().method().compareTo(HttpMethod.POST) == 0
                || context.request().method().compareTo(HttpMethod.PUT) == 0)
                && !StringUtils.startsWithIgnoreCase(context.request().getHeader("Content-Type"), "application/json")){
            LOGGER.error("请求方式不正确【{}】", context.request().getHeader("Content-Type"));
            badRequest(context, new Throwable("请求方式【Content-Type】错误"));
            return;
        }
        context.next();
    }

    private void authUaaHandler(RoutingContext context) {
        authHandler.auth(context.request().getHeader("token"), handler -> {
            if (handler.failed()) {
                LOGGER.error("no login: ", handler.cause());
                this.noAuth(context);
                return ;
            }
            if (StringUtils.isEmpty(handler.result())) {
                LOGGER.error("no login, user is empty!");
                this.noAuth(context);
                return ;
            }
            this.returnWithSuccessMessage(context, handler.result());
            return ;
        });
    }

    /**
     * 登录
     * @param context 上下文
     */
    private void loginEntryHandler(RoutingContext context) {
        final JsonObject params = context.getBodyAsJson();
        final String loginName = params.getString("loginName");
        LOGGER.info("用户【{}】登录系统", loginName);
        authHandler.login(new JsonObject().put("status", UserStatus.ACTIVATION.getStatus())
                .put("loginName", loginName).put("password", params.getString("pwd")), handler -> {
            if (handler.succeeded()) {
                JsonObject userSession = handler.result();
                if(Objects.isNull(userSession)){
                    LOGGER.info("用户【{}】登录，用户不存在", loginName);
                    this.returnWithFailureMessage(context, "用户名或密码错误");
                } else {
                    LOGGER.info("用户【{}】登录成功", userSession);
                    this.returnWithSuccessMessage(context, "登录成功", userSession);
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
        //context.response().setStatusCode(204).end();
        this.returnWithSuccessMessage(context, "注销成功");
    }

    private void uploadImageAvatarHandler(RoutingContext context){
        if (Objects.isNull(context.user())){
            this.noAuth(context);
            return;
        }
        Set<FileUpload> fileUploads = context.fileUploads();
        if(CollectionUtil.isEmpty(fileUploads)){
            LOGGER.error("头像上传失败，文件为空！");
            this.returnWithFailureMessage(context, "上传失败！");
            return ;
        }
        for (FileUpload avatar : fileUploads) {
            FileSystem fs = vertx.fileSystem();
            final String[] images = StringUtils.split(avatar.fileName(), ".");
            String fileName = UuidUtil.createClusterUuid() + "." + images[images.length - 1];
            fs.copy(avatar.uploadedFileName(), "/data/shop/images/avatar/" + fileName, res -> {
                if (res.succeeded()) {
                    this.returnWithSuccessMessage(context, "上传成功！", "http://111.231.132.168:8080/images/avatar/" + fileName);
                } else {
                    LOGGER.error("头像上传失败！", res.cause());
                    this.returnWithFailureMessage(context, "上传失败，请重试！");
                }
            });
        }
    }



}
