package com.ecit.service.impl;

import com.ecit.service.ICartService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * Created by za-wangshenhua on 2018/4/6.
 */
public class CartServiceImpl implements ICartService {


    public CartServiceImpl(Vertx vertx, JsonObject config) {}

    @Override
    public ICartService insertOrder() {
        return null;
    }
}
