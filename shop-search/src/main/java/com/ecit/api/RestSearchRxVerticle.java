package com.ecit.api;

import com.ecit.SearchType;
import com.ecit.common.constants.Constants;
import com.ecit.common.result.ResultItems;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.service.ICommodityService;
import com.ecit.service.IPreferencesService;
import com.google.common.collect.Lists;
import com.hubrick.vertx.elasticsearch.model.Hits;
import com.hubrick.vertx.elasticsearch.model.SearchResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestSearchRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestSearchRxVerticle.class);
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
        router.route().handler(CookieHandler.create());
        // API route handler
        router.post("/search").handler(this::searchHandler);
        router.get("/findCommodityById/:id").handler(this::findCommodityByIdHandler);
        router.get("/findFavoriteCommodity").handler(this::findFavoriteCommodityHandler);
        router.get("/findBrandCategory").handler(this::findBrandCategoryHandler);
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("search.http.address", "localhost");
        int port = config().getInteger("search.http.port", 8082);

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
        String cookie = this.getHeader(context, Constants.VERTX_WEB_SESSION);
        if(StringUtils.isNotEmpty(cookie) && StringUtils.isNotEmpty(keyword)){
            preferencesService.sendMqPreferences(cookie, keyword, SearchType.search, handler ->{});
        }
        commodityService.searchCommodity(keyword, handler -> {
            if(handler.failed()){
                this.returnWithFailureMessage(context, "暂无该商品！");
                LOGGER.error("搜索商品异常：", handler.cause());
                return ;
            } else {
                if(Objects.isNull(handler.result())){
                    this.returnWithFailureMessage(context, "暂无该商品！");
                    return ;
                }
                final SearchResponse result = handler.result();
                JsonObject resultJsonObject = new JsonObject();
                resultJsonObject.put("items", result.getHits().getHits().stream().map(hit -> hit.getSource()).collect(Collectors.toList()));
                JsonArray category = result.getAggregations().get("category_name").getJsonObject("category_name").getJsonArray("buckets");
                List<String> categoryList = Lists.newArrayList();
                for (int i = 0; i < category.size(); i++) {
                    categoryList.add(category.getJsonObject(i).getString("key"));
                }
                resultJsonObject.put("category", categoryList);
                JsonArray brand = result.getAggregations().get("brand_name").getJsonObject("brand_name").getJsonArray("buckets");
                List<String> brandList = Lists.newArrayList();
                for (int i = 0; i < brand.size(); i++) {
                    brandList.add(brand.getJsonObject(i).getString("key"));
                }
                resultJsonObject.put("brand", brandList);
                this.Ok(context, new ResultItems(0, result.getHits().getTotal().intValue(),
                        resultJsonObject));
            }
        });
    }

    /**
     * 根据商品id查询详情信息
     * @param context
     */
    private void findCommodityByIdHandler(RoutingContext context){
        commodityService.findCommodityById(Integer.parseInt(context.request().getParam("id")), handler -> {
            if (handler.failed()) {
                LOGGER.error("根据id查询产品失败！", handler.cause());
                this.returnWithFailureMessage(context, "查询失败");
            } else {
                final Hits hits = handler.result().getHits();
                this.Ok(context, ResultItems.getReturnItemsSuccess(hits.getTotal().intValue(), hits.getHits().get(0).getSource()));
            }
        });
    }

    /**
     * 猜想喜欢的商品
     * @param context
     */
    private void findFavoriteCommodityHandler(RoutingContext context){
        String cookie = this.getHeader(context, Constants.VERTX_WEB_SESSION);
        if(StringUtils.isEmpty(cookie)){
            //查询默认的
            Future<SearchResponse> commodityFuture = Future.future();
            commodityService.findCommodityBySalesVolume(commodityFuture.completer());
            commodityFuture.setHandler(res -> {
                if (res.failed()) {
                    LOGGER.error("查询偏好产品失败！", res.cause());
                    this.returnWithFailureMessage(context, "查询失败！");
                    return;
                }
                this.Ok(context, ResultItems.getReturnItemsSuccess(res.result().getHits().getTotal().intValue(),
                        res.result().getHits().getHits().stream().map(hit -> hit.getSource()).collect(Collectors.toList())));
            });
            return ;
        }
        Future<List<String>> future = Future.future();
        preferencesService.findPreferences(cookie, future.completer());
        future.compose(list -> {
            Future<SearchResponse> commodityFuture = Future.future();
            commodityService.preferencesCommodity(list, commodityFuture.completer());
            return commodityFuture;
        }).setHandler(res -> {
            if (res.failed()) {
                LOGGER.error("查询偏好产品失败！", res.cause());
                this.returnWithFailureMessage(context, "查询失败！");
                return;
            }
            this.Ok(context, new ResultItems(0, res.result().getHits().getTotal().intValue(),
                    res.result().getHits().getHits().stream().map(hit -> hit.getSource()).collect(Collectors.toList())));
        });
    }

    /**
     * 根据查询关键字聚合品牌、类别
     * @param context
     */
    public void findBrandCategoryHandler(RoutingContext context){
        String keyword = context.request().getParam("key");
        commodityService.findBrandCategory(keyword, handler -> {
            if(handler.failed()){
                this.returnWithFailureMessage(context, "暂无该商品！");
                LOGGER.error("搜索商品异常：", handler.cause());
                return ;
            } else {
                if(Objects.isNull(handler.result())){
                    this.returnWithFailureMessage(context, "暂无该商品！");
                    return ;
                }
                Map<String, JsonObject> aggs = handler.result().getAggregations();
                System.out.println(aggs.get("category_name").getJsonObject("category_name").getJsonArray("buckets"));
                System.out.println(aggs.get("brand_name").getJsonObject("brand_name").getJsonArray("buckets"));
                this.Ok(context, new ResultItems(0, handler.result().getHits().getTotal().intValue(),
                        handler.result().getHits().getHits().stream().map(hit -> hit.getSource()).collect(Collectors.toList())));
            }
        });
    }

}
