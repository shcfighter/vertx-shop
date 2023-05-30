package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.utils.JsonUtils;
import com.ecit.constants.UserSql;
import com.ecit.handler.IAddressHandler;
import com.ecit.handler.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            Promise<Integer> promise = Promise.promise();
            this.execute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(userId)
                            .addString(params.getString("receiver")).addString(params.getString("mobile")).addString(params.getString("province_code"))
                            .addString(params.getString("city_code")).addString(params.getString("county_code"))
                            .addString(params.getString("address")).addString(params.getString("address_details"))
                    , UserSql.INSERT_ADDRESS_SQL)
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IAddressHandler updateAddressHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addString(params.getString("receiver")).addString(params.getString("mobile"))
                                .addString(params.getString("province_code")).addString(params.getString("city_code"))
                                .addString(params.getString("county_code")).addString(params.getString("address"))
                                .addString(params.getString("address_details")).addLong(params.getLong("address_id"))
                , UserSql.UPDATE_ADDRESS_BY_ID_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public IAddressHandler updateDefaultAddress(String token, long addressId, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            Promise<Integer> promise = Promise.promise();
            pgPool.rxGetConnection()
                    .flatMap(conn -> conn.preparedQuery(UserSql.UPDATE_ADDRESS_BY_NOT_DEFAULT_SQL)
                            .rxExecute(Tuple.tuple().addLong(session.getLong("userId")))
                            .flatMap(update -> conn.preparedQuery(UserSql.UPDATE_ADDRESS_BY_DEFAULT_SQL)
                                    .rxExecute(Tuple.tuple().addLong(addressId))
                            )
                            .doAfterTerminate(conn::close)
            ).subscribe(rs -> promise.complete(rs.rowCount()), promise::fail);
            return promise.future();
        }).onComplete(handler);
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
            Promise<List<JsonObject>> promise = Promise.promise();
            this.retrieveMany(Tuple.tuple().addLong(session.getLong("userId")), UserSql.FIND_ADDRESS_SQL)
                    .subscribe(promise::complete, promise::fail);
            return promise.future();
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IAddressHandler deleteAddress(long addressId, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addLong(addressId), UserSql.DELETE_ADDRESS_BY_ID_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public IAddressHandler getAddressById(long addressId, Handler<AsyncResult<JsonObject>> handler) {
        Promise<JsonObject> promise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addLong(addressId), UserSql.GET_ADDRESS_BY_ID_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }
}
