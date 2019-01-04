//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ecit.common.auth.impl;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.result.ResultItems;
import com.ecit.common.utils.JsonUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShopUserSessionHandlerImpl extends JdbcRxRepositoryWrapper implements ShopUserSessionHandler {

    private static final Logger LOGGER = LogManager.getLogger(ShopUserSessionHandlerImpl.class);

    public ShopUserSessionHandlerImpl(Vertx vertx, JsonObject config) {
        super(io.vertx.reactivex.core.Vertx.newInstance(vertx), config);
    }

    public void handle(RoutingContext routingContext) {
        String token = routingContext.request().getHeader("token");
        LOGGER.debug("token: {}", token);
        Future<JsonObject> future = this.getSession(token);
        future.compose(user -> {
            LOGGER.info("session user: {}", user);
            if (JsonUtils.isNull(user)) {
                return Future.failedFuture("can not get session");
            }
            return Future.succeededFuture();
        }).setHandler(handler -> {
            if(handler.failed()){
                this.noAuth(routingContext);
                return ;
            }
        });
        routingContext.next();
    }

    private void noAuth(RoutingContext routingContext){
        routingContext.response().setStatusCode(401)
                .putHeader("content-type", "application/json")
                .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure("no_auth")));
		return ;
    }
}
