package com.ecit.service;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
@VertxGen
@ProxyGen
public interface IOrderService {

    String ORDER_SERVICE_ADDRESS = "order_service_address";

    @Fluent
    IOrderService insertOrder();
}
