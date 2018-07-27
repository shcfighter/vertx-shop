package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.handler.ICollectionHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by shwang on 2018/2/2.
 */
public class RestCollectionRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestCollectionRxVerticle.class);
    private static final String HTTP_COLLECTION_SERVICE = "http_collection_service_api";
    private ICollectionHandler collectionHandler;

    @Override
    public void start() throws Exception {
        super.start();
        this.collectionHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICollectionHandler.COLLECTION_SERVICE_ADDRESS).build(ICollectionHandler.class);
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post("/insertCollection").handler(context -> this.requireLogin(context, this::insertCollectionHandler));
        router.get("/findCollection").handler(context -> this.requireLogin(context, this::findMessageHandler));
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("collection.http.address", "localhost");
        int port = config().getInteger("collection.http.port", 8086);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_COLLECTION_SERVICE, host, port, "collection.api.name").subscribe();
            LOGGER.info("shop-collection server started!");
        }, error -> {
            LOGGER.info("shop-collection server start fail!", error);
        });
    }

    /**
     *  保存收藏商品
     * @param context
     */
    private void insertCollectionHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        JsonObject params = context.getBodyAsJson();
        params.put("user_id", userId);
        params.put("is_deleted", 0);
        params.put("create_time", System.currentTimeMillis());
        collectionHandler.sendCollection(params, handler ->{
            if(handler.succeeded()){
                LOGGER.info("商品收藏成功，code:{}", handler.result());
                this.returnWithSuccessMessage(context, "商品收藏成功");
            } else {
                LOGGER.error("商品收藏失败", handler.cause());
                this.returnWithFailureMessage(context, "商品收藏失败");
            }
        });
    }

    private void findMessageHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        collectionHandler.findCollection(userId, Integer.parseInt(context.request().getParam("pageNum")), handler ->{
            if(handler.succeeded()){
                this.returnWithSuccessMessage(context, "查询收藏成功", handler.result());
            } else {
                LOGGER.info("查询收藏失败", handler.cause());
                this.returnWithFailureMessage(context, "查询收藏失败");
            }
        });
    }

}
