package com.ecit.service.impl;

import com.ecit.SearchType;
import com.ecit.service.IPreferencesService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by za-wangshenhua on 2018/3/22.
 */
public class PreferencesServiceImpl implements IPreferencesService{

    final MongoClient mongoClient;
    /**
     * mongodb的collection名称
     */
    private static final String PREFERENCES_COLLECTION = "preferences";

    public PreferencesServiceImpl(Vertx vertx, JsonObject config) {
        mongoClient = MongoClient.createShared(vertx, config);
    }

    /**
     * 保存查询关键字
     * @param cookies
     * @param keyword
     * @param searchType
     * @return
     */
    @Override
    public IPreferencesService savePreferences(String cookies, String keyword, SearchType searchType, Handler<AsyncResult<String>> handler) {
        Future<String> future = Future.future();
        if(StringUtils.isEmpty(cookies)){
            future = Future.failedFuture(new NullPointerException("cookies 为null"));
            return this;
        }
        mongoClient.rxInsert(PREFERENCES_COLLECTION, new JsonObject()
                .put("cookies", cookies)
                .put("keyword", keyword)
                .put("search_type", searchType.name())
                .put("create_time", new Date())).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    /**
     * 历史查询关键字
     * @param cookies
     * @param handler
     * @return
     */
    @Override
    public IPreferencesService findPreferences(String cookies, Handler<AsyncResult<List<String>>> handler) {
        Future<List<String>> future = Future.future();
        mongoClient.rxFindWithOptions(PREFERENCES_COLLECTION, new JsonObject().put("cookies", cookies),
                new FindOptions().setLimit(10).setSort(new JsonObject().put("create_time", -1)))
                .subscribe(res -> future.complete(res.stream().map(jsonObject -> jsonObject.getString("keyword")).collect(Collectors.toList())), future::fail);
        future.setHandler(handler);
        return this;
    }
}
