package com.ecit.service.impl;

import com.ecit.common.db.JdbcRepositoryWrapper;
import com.ecit.service.IOrderService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/4/5.
 */
public class OrderServiceImpl extends JdbcRepositoryWrapper implements IOrderService {

    public OrderServiceImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
    }

    @Override
    public IOrderService insertOrder() {
        return this;
    }
}
