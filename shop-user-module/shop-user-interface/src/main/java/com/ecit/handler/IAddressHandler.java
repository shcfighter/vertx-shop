package com.ecit.handler;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;

@ProxyGen
@VertxGen
public interface IAddressHandler {

    public static final String ADDRESS_SERVICE_ADDRESS = "address-handler-address";

    @Fluent
    IAddressHandler insertAddressHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler updateAddressHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler updateDefaultAddress(String token, long addressId, Handler<AsyncResult<UpdateResult>> handler);

    @Fluent
    IAddressHandler findAddress(String token, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IAddressHandler deleteAddress(long addressId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler getAddressById(long addressId, Handler<AsyncResult<JsonObject>> handler);
}
