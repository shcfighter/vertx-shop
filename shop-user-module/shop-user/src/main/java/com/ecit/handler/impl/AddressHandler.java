package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.constants.UserSql;
import com.ecit.handler.IAddressHandler;
import com.ecit.handler.IdBuilder;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AddressHandler extends JdbcRxRepositoryWrapper implements IAddressHandler {
    private static final Logger LOGGER = LogManager.getLogger(AddressHandler.class);

    public AddressHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IAddressHandler insertAddressHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            long userId = session.getLong("userId");
            Future<Integer> future = Future.future();
            this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId)
                            .add(params.getString("receiver")).add(params.getString("mobile")).add(params.getString("province_code"))
                            .add(params.getString("city_code")).add(params.getString("county_code"))
                            .add(params.getString("address")).add(params.getString("address_details"))
                    , UserSql.INSERT_ADDRESS_SQL)
                    .subscribe(future::complete, future::fail);
            return future;
        });
        resultFuture.setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler updateAddressHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(params.getString("receiver")).add(params.getString("mobile")).add(params.getString("province_code"))
                        .add(params.getString("city_code")).add(params.getString("county_code"))
                        .add(params.getString("address")).add(params.getString("address_details")).add(params.getLong("address_id"))
                , UserSql.UPDATE_ADDRESS_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler updateDefaultAddress(String token, long addressId, Handler<AsyncResult<UpdateResult>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<UpdateResult> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            Future<UpdateResult> future = Future.future();
            this.getConnection().flatMap(conn ->
                    conn.rxSetAutoCommit(false).toSingleDefault(false)
                            .flatMap(autoCommit -> conn.rxUpdateWithParams(UserSql.UPDATE_ADDRESS_BY_NOT_DEFAULT_SQL
                                    , new JsonArray().add(session.getLong("userId"))))
                            .flatMap(update -> conn.rxUpdateWithParams(UserSql.UPDATE_ADDRESS_BY_DEFAULT_SQL
                                    , new JsonArray().add(addressId)))
                            .onErrorResumeNext(ex -> conn.rxRollback()
                                    .toSingleDefault(true)
                                    .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                                    .flatMap(ignore -> Single.error(ex))
                            )
                            .doAfterTerminate(conn::close)
            ).subscribe(future::complete, future::fail);
            return future;
        });
        resultFuture.setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler findAddress(String token, Handler<AsyncResult<List<JsonObject>>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<List<JsonObject>> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            Future<List<JsonObject>> future = Future.future();
            this.retrieveMany(new JsonArray().add(session.getLong("userId")), UserSql.FIND_ADDRESS_SQL)
                    .subscribe(future::complete, future::fail);
            return future;
        });
        resultFuture.setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler deleteAddress(long addressId, Handler<AsyncResult<Integer>> handler) {
        Future<Integer> future = Future.future();
        this.execute(new JsonArray().add(addressId), UserSql.DELETE_ADDRESS_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public IAddressHandler getAddressById(long addressId, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> future = Future.future();
        this.retrieveOne(new JsonArray().add(addressId), UserSql.GET_ADDRESS_BY_ID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
