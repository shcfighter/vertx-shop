package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
@VertxGen
@ProxyGen
public interface ICartService {

    String CART_SERVICE_ADDRESS = "cart_service_address";

    @Fluent
    ICartService insertOrder();
}
