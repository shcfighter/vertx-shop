package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.constants.UserSql;
import com.ecit.service.IAddressService;
import com.ecit.service.IdBuilder;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;

import java.util.List;

public class AddressServiceImpl extends JdbcRxRepositoryWrapper implements IAddressService {

    public AddressServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IAddressService insertAddress(long userId, String receiver, String mobile, String provinceCode, String cityCode, String countyCode, String address, String addressDetails, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId)
                .add(receiver).add(mobile).add(provinceCode).add(cityCode).add(countyCode)
                .add(address).add(addressDetails)
                , UserSql.INSERT_ADDRESS_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressService updateAddress(long addressId, String receiver, String mobile, String provinceCode, String cityCode, String countyCode, String address, String addressDetails, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(receiver).add(mobile).add(provinceCode).add(cityCode).add(countyCode)
                        .add(address).add(addressDetails).add(addressId)
                , UserSql.UPDATE_ADDRESS_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressService updateDefaultAddress(long userId, long addressId, Handler<AsyncResult<UpdateResult>> handler) {
        Future<UpdateResult> future = Future.future();
        this.getConnection().flatMap(conn ->
            conn.rxSetAutoCommit(false).toSingleDefault(false)
                    .flatMap(autoCommit -> conn.rxUpdateWithParams(UserSql.UPDATE_ADDRESS_BY_NOT_DEFAULT_SQL
                            , new JsonArray().add(userId)))
                    .flatMap(update -> conn.rxUpdateWithParams(UserSql.UPDATE_ADDRESS_BY_DEFAULT_SQL
                            , new JsonArray().add(addressId)))
                    .onErrorResumeNext(ex -> conn.rxRollback()
                            .toSingleDefault(true)
                            .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                            .flatMap(ignore -> Single.error(ex))
                    )
                    .doAfterTerminate(conn::close)
        ).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressService findAddress(long userId, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<List<JsonObject>> future = Future.future();
        this.retrieveMany(new JsonArray().add(userId), UserSql.FIND_ADDRESS_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressService deleteAddress(long addressId, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(addressId), UserSql.DELETE_ADDRESS_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressService getAddressById(long addressId, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(addressId), UserSql.GET_ADDRESS_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
