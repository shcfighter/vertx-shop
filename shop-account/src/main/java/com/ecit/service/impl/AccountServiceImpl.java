package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class AccountServiceImpl extends JdbcRepositoryWrapper{

    public AccountServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }
}
