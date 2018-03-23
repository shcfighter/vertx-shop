package com.ecit.service;

import com.ecit.SearchType;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Future;

import java.util.List;

/**
 * Created by za-wangshenhua on 2018/3/22.
 */
@ProxyGen
@VertxGen
public interface IPreferencesService {

    @Fluent
    IPreferencesService savePreferences(String cookies, String keyword, SearchType searchType, Handler<AsyncResult<String>> handler);

    @Fluent
    IPreferencesService findPreferences(String cookies, Handler<AsyncResult<List<String>>> handler);
}
