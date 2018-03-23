package com.ecit.api;

import com.ecit.SearchType;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.ICommodityService;
import com.ecit.service.IPreferencesService;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.ext.web.Cookie;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestSearchRxVerticle extends RestAPIRxVerticle{

    private static final String HTTP_SEARCH_SERVICE = "http_search_service_api";
    private final ICommodityService commodityService;
    private final IPreferencesService preferencesService;

    public RestSearchRxVerticle(ICommodityService commodityService, IPreferencesService preferencesService) {
        this.commodityService = commodityService;
        this.preferencesService = preferencesService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.post("/search").handler(this::searchHandler);
        router.get("/findCommodityById/:id").handler(this::findCommodityByIdHandler);
        router.get("/findFavoriteCommodity").handler(this::findFavoriteCommodityHandler);
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
        final String keyword = context.request().getParam("keyword");
        /**
         * 异步保存搜索行为
         */
        Cookie cookie = context.getCookie("vertx-web.session");
        if(Objects.nonNull(cookie)){
            preferencesService.savePreferences(cookie.getValue(), keyword, SearchType.search, handler ->{});
        }
        commodityService.searchCommodity(keyword, handler -> {
            if(handler.failed()){
                LOGGER.error("搜索商品异常：", handler.cause());
            } else {
                this.Ok(context, new ResultItems(0, handler.result().getHits().getTotal().intValue(),
                        handler.result().getHits().getHits().get(0).getSource()));
            }
        });
    }

    /**
     * 根据商品id查询详情信息
     * @param context
     */
    private void findCommodityByIdHandler(RoutingContext context){
        commodityService.findCommodityById(Integer.parseInt(context.request().getParam("id"))
                , this.resultHandler(context));
    }

    /**
     * 猜想喜欢的商品
     * @param context
     */
    private void findFavoriteCommodityHandler(RoutingContext context){
        final String cookies = context.getCookie("vertx-web.session").getValue();
        if(StringUtils.isEmpty(cookies)){
            Future.failedFuture(new NullPointerException("查询条件为null"));
        }
        Future<List<String>> future = Future.future();
        preferencesService.findPreferences(cookies, future.completer());
        future.compose(list -> {
            commodityService.preferencesCommodity(list, handler -> {
                if(handler.failed()){
                    LOGGER.error("搜索商品异常：", handler.cause());
                } else {
                    this.Ok(context, new ResultItems(0, handler.result().getHits().getTotal().intValue(),
                            handler.result().getHits().getHits().get(0).getSource()));
                }
            });
            return Future.succeededFuture();
        });
    }

}
