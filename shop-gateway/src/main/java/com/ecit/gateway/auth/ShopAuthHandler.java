//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ecit.gateway.auth;

import com.ecit.gateway.auth.impl.ShopAuthHandlerImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

@VertxGen
public interface ShopAuthHandler {

    static ShopAuthHandler create(Vertx vertx, JsonObject config) {
        return new ShopAuthHandlerImpl(vertx, config);
    }

    @Fluent
    ShopAuthHandler login(JsonObject params, Handler<AsyncResult<JsonObject>> handler);

    @Fluent
    ShopAuthHandler logout(String token, Handler<AsyncResult<Long>> handler);

    @Fluent
    ShopAuthHandler auth(String token, Handler<AsyncResult<String>> handler);

}
