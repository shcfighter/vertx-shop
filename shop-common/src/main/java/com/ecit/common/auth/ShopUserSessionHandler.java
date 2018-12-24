package com.ecit.common.auth;

import com.ecit.common.auth.impl.ShopUserSessionHandlerImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@VertxGen
public interface ShopUserSessionHandler extends Handler<RoutingContext> {
    static ShopUserSessionHandler create(Vertx vertx, JsonObject config) {
        return new ShopUserSessionHandlerImpl(vertx, config);
    }
}