package com.ecit.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Created by za-wangshenhua on 2018/3/15.
 */
@ProxyGen
@VertxGen
public interface ICommodityService {

    public static final String SEARCH_SERVICE_ADDRESS = "search_service_address";

    void insertCommodity();
}
