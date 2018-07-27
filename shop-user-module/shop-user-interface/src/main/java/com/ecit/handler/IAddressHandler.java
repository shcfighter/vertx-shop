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
    IAddressHandler insertAddress(long userId, String receiver, String mobile, String provinceCode, String cityCode, String countyCode, String address, String addressDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler updateAddress(long addressId, String receiver, String mobile, String provinceCode, String cityCode, String countyCode, String address, String addressDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler updateDefaultAddress(long userId, long addressId, Handler<AsyncResult<UpdateResult>> handler);

    @Fluent
    IAddressHandler findAddress(long userId, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IAddressHandler deleteAddress(long addressId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressHandler getAddressById(long addressId, Handler<AsyncResult<JsonObject>> handler);
}
