package com.ecit.service.impl;

import com.ecit.service.ICommodityService;
import com.hubrick.vertx.elasticsearch.ElasticSearchService;
import com.hubrick.vertx.elasticsearch.RxElasticSearchService;
import com.hubrick.vertx.elasticsearch.model.IndexOptions;
import com.hubrick.vertx.elasticsearch.model.OpType;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/3/15.
 */
public class CommodityServiceImpl implements ICommodityService {

    final ElasticSearchService elasticSearchService;
    //final RxElasticSearchService rxElasticSearchService;

    public CommodityServiceImpl(Vertx vertx, JsonObject config) {
        elasticSearchService = ElasticSearchService.createEventBusProxy(vertx.getDelegate(), config.getString("address"));
        //rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx.getDelegate(), "eventbus-address");
    }


    @Override
    public void insertCommodity() {
        final IndexOptions indexOptions = new IndexOptions()
                .setId("123")
                .setOpType(OpType.INDEX);

        elasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexOptions, indexResponse -> {
            // Do something
            if(indexResponse.succeeded()){
                System.out.println(indexResponse.result());
            } else {
                System.out.println(indexResponse.cause());
            }
        });
    }
}
