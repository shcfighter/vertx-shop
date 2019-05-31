
package com.ecit.gateway.auth.impl;

import com.ecit.common.constants.Constants;
import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.enums.IsDeleted;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.constants.UserSql;
import com.ecit.gateway.auth.ShopAuthHandler;
import com.hazelcast.util.MD5Util;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShopAuthHandlerImpl extends JdbcRxRepositoryWrapper implements ShopAuthHandler {
    private static final Logger LOGGER = LogManager.getLogger(ShopAuthHandlerImpl.class);
    private ShopHashStrategy strategy;

    public ShopAuthHandlerImpl(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.strategy = new DefaultHashStrategy(vertx.getDelegate());
    }

    @Override
    public ShopAuthHandler login(JsonObject params, Handler<AsyncResult<JsonObject>> resultHandler) {
        String username = params.getString("loginName");
        if (username == null) {
            resultHandler.handle(Future.failedFuture("authInfo must contain username in 'loginName' field"));
        } else {
            String password = params.getString("password");
            if (password == null) {
                resultHandler.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
            } else {
                this.retrieveMany(new JsonArray().add(username).add(username).add(username).add(Integer.valueOf(IsDeleted.NO.getValue())), UserSql.LOGIN_SQL)
                        .subscribe(rs -> {
                            switch (rs.size()) {
                                case 0: {
                                    resultHandler.handle(Future.succeededFuture());
                                    break;
                                }
                                case 1: {
                                    JsonObject row = rs.get(0);
                                    System.out.println(row);
                                    String hashedStoredPwd = this.strategy.getHashedStoredPwd(row);
                                    String salt = this.strategy.getSalt(row);
                                    int version = -1;
                                    int sep = hashedStoredPwd.lastIndexOf(36);
                                    if (sep != -1) {
                                        try {
                                            version = Integer.parseInt(hashedStoredPwd.substring(sep + 1));
                                        } catch (NumberFormatException var11) {
                                            resultHandler.handle(Future.failedFuture(("Invalid nonce version: " + version)));
                                            return;
                                        }
                                    }
                                    if (hashedStoredPwd.equals(this.strategy.computeHash(password, salt, version))) {
                                        JsonObject user = new JsonObject().put("userId", Long.parseLong(row.getString("userid"))).put("loginName", row.getString("loginname"));
                                        final String token = MD5Util.toMD5String(StringUtils.join(user.getString("userid"), Constants.UNDERLINE, user.getString("loginname"), Constants.UNDERLINE, System.currentTimeMillis()));
                                        this.setSession(token, user);
                                        user.put(Constants.TOKEN, token);
                                        resultHandler.handle(Future.succeededFuture(user));
                                        break;
                                    }
                                    resultHandler.handle(Future.succeededFuture());
                                    break;
                                }
                                default: {
                                    resultHandler.handle(Future.failedFuture("Failure in authentication"));
                                }
                            }
                        }, fail -> {
                            resultHandler.handle(Future.failedFuture(fail.getCause()));
                        });
            }
        }
        return this;
    }

    @Override
    public ShopAuthHandler logout(String token, Handler<AsyncResult<Long>> handler) {
        if(StringUtils.isEmpty(token)){
            handler.handle(Future.failedFuture("token empty"));
            return this;
        }
        Future<Long> future = Future.future();
        redisClient.rxHdel(Constants.VERTX_WEB_SESSION, token).subscribe(future::complete, future::fail);
        future.setHandler(handler);
        return this;
    }

    @Override
    public ShopAuthHandler auth(String token, Handler<AsyncResult<String>> handler) {
        if(StringUtils.isEmpty(token)){
            handler.handle(Future.failedFuture("token empty!"));
            return this;
        }
        Future<String> future = Future.future();
        redisClient.hget(Constants.VERTX_WEB_SESSION, token, re -> {
          if (re.failed()) {
              LOGGER.error("redis hget : ", re.cause());
              future.fail(re.cause());
          } else {
              LOGGER.info("redis info: ", re.result());
              future.complete(re.result());
          }
        });
        future.setHandler(handler);
        return this;
    }

}
