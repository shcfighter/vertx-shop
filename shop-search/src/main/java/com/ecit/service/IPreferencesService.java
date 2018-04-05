package com.ecit.service;

import com.ecit.SearchType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.codegen.annotations.*;
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

    @GenIgnore
    @ProxyIgnore
    Completable savePreferences();

    @Fluent
    IPreferencesService findPreferences(String cookies, Handler<AsyncResult<List<String>>> handler);

    @Fluent
    IPreferencesService sendMqPreferences(String cookies, String keyword, SearchType searchType, Handler<AsyncResult<String>> handler);
}
