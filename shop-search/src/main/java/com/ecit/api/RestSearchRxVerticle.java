package com.ecit.api;

import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.ICommodityService;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import io.vertx.core.Future;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestSearchRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestSearchRxVerticle.class);
    private static final String HTTP_SEARCH_SERVICE = "http_search_service_api";
    private final ICommodityService commodityService;

    public RestSearchRxVerticle(ICommodityService commodityService) {
        this.commodityService = commodityService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.get("/search").handler(this::searchHandler);
        router.get("/findCommodityById/:id").handler(this::findCommodityByIdHandler);
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("user.http.address", "localhost");
        int port = config().getInteger("user.http.port", 8082);

        // create HTTP server and publish REST service
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_SEARCH_SERVICE, host, port).subscribe();
            LOGGER.info("shop-search server started!");
        }, error -> {
            LOGGER.info("shop-search server start fail!", error);
        });
    }

    /**
     * 搜索商品信息
     * @param context
     */
    private void searchHandler(RoutingContext context){
        commodityService.searchCommodity(context.request().getParam("key"), handler -> {
            if(handler.failed()){
                LOGGER.error("搜索商品异常：", handler.cause());
            } else {
                this.Ok(context, new ResultItems(0, handler.result().getHits().getTotal().intValue(),
                        handler.result().getHits().getHits().get(0).getSource()));
            }
        });
    }

    private void findCommodityByIdHandler(RoutingContext context){
        commodityService.findCommodityById(Integer.parseInt(context.request().getParam("id"))
                , this.resultHandler(context));
    }

}
