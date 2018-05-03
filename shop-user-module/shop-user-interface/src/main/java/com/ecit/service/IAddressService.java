package com.ecit.service;

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
public interface IAddressService {

    @Fluent
    IAddressService insertAddress(long userId, String receiver, String mobile, String provinceCode, String cityCode, String countyCode, String address, String addressDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressService updateAddress(long addressId, String receiver, String mobile, String provinceCode, String cityCode, String countyCode, String address, String addressDetails, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressService updateDefaultAddress(long userId, long addressId, Handler<AsyncResult<UpdateResult>> handler);

    @Fluent
    IAddressService findAddress(long userId, Handler<AsyncResult<List<JsonObject>>> handler);

    @Fluent
    IAddressService deleteAddress(long addressId, Handler<AsyncResult<Integer>> handler);

    @Fluent
    IAddressService getAddressById(long addressId, Handler<AsyncResult<JsonObject>> handler);
}
