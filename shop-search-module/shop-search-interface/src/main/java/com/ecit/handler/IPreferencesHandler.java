package com.ecit.handler;

import com.ecit.SearchType;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Created by shwang on 2018/3/22.
 */
@ProxyGen
@VertxGen
public interface IPreferencesHandler {

    String SEARCH_SERVICE_PREFERENCES = "search_service_preferences";

    @Fluent
    IPreferencesHandler savePreferences(Handler<AsyncResult<Void>> handler);

    @Fluent
    IPreferencesHandler findPreferences(String cookies, Handler<AsyncResult<List<String>>> handler);

    @Fluent
    IPreferencesHandler sendMqPreferences(String cookies, String keyword, SearchType searchType, Handler<AsyncResult<String>> handler);
}
