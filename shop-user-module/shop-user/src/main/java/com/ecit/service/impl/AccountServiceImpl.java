package com.ecit.service.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.constants.AccountSql;
import com.ecit.service.IAccountService;
import com.ecit.service.IdBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AccountServiceImpl extends JdbcRxRepositoryWrapper implements IAccountService {

    private static final Logger LOGGER = LogManager.getLogger(AccountServiceImpl.class);

    public AccountServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    /**
     * 初始化用户资金账户
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public IAccountService insertAccount(long userId, Handler<AsyncResult<Integer>> handler) {
        Future future = Future.future();
        this.execute(new JsonArray().add(IdBuilder.getUniqueId()).add(userId), AccountSql.INSERT_ACCOUNT_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 根据用户id查询资金账户
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public IAccountService findAccount(long userId, Handler<AsyncResult<JsonObject>> handler) {
        Future future = Future.future();
        this.retrieveOne(new JsonArray().add(userId), AccountSql.FIND_ACCOUNT_BY_USERID_SQL)
                .subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }
}
