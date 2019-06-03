package com.ecit.api;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.constants.Constants;
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

        /**
         * 登录拦截
         */
        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        // API route handler
        router.post("/insertCollection").handler(this::insertCollectionHandler);
        router.get("/findCollection").handler(this::findCollectionHandler);
        router.delete("/removeCollection/:id").handler(this::removeCollectionHandler);
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
    private void insertCollectionHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        JsonObject params = context.getBodyAsJson();
        params.put("is_deleted", 0);
        params.put("create_time", System.currentTimeMillis());
        collectionHandler.sendCollection(token, params, handler ->{
            if(handler.succeeded()){
                LOGGER.info("商品收藏成功，code:{}", handler.result());
                this.returnWithSuccessMessage(context, "商品收藏成功");
            } else {
                LOGGER.error("商品收藏失败", handler.cause());
                this.returnWithFailureMessage(context, "商品收藏失败");
            }
        });
    }

    private void findCollectionHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        collectionHandler.findCollection(token, Integer.parseInt(context.request().getParam("pageNum")), handler ->{
            if(handler.succeeded()){
                this.returnWithSuccessMessage(context, "查询收藏成功", handler.result());
            } else {
                LOGGER.info("查询收藏失败", handler.cause());
                this.returnWithFailureMessage(context, "查询收藏失败");
            }
        });
    }

    private void removeCollectionHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        collectionHandler.removeCollection(token, context.request().getParam("id"), handler ->{
            if(handler.succeeded() && handler.result().getDocModified() > 0){
                this.returnWithSuccessMessage(context, "取消收藏成功", handler.result());
            } else {
                LOGGER.info("取消收藏失败", handler.cause());
                this.returnWithFailureMessage(context, "取消收藏失败");
            }
        });
    }

}
