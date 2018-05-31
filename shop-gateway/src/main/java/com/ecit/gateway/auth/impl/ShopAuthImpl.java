
package com.ecit.gateway.auth.impl;

import com.ecit.common.enums.IsDeleted;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.gateway.auth.ShopAuth;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.util.function.Consumer;

public class ShopAuthImpl implements AuthProvider, ShopAuth {
    private JDBCClient client;
    private String authenticateQuery = "select user_id userId, login_name loginName, password pwd, salt, status from t_user where (mobile = ? or email = ? or login_name = ?) and is_deleted = ?";
    private String rolesQuery = "SELECT ROLE FROM USER_ROLES WHERE USERNAME = ?";
    private String permissionsQuery = "SELECT PERM FROM ROLES_PERMS RP, USER_ROLES UR WHERE UR.USERNAME = ? AND UR.ROLE = RP.ROLE";
    private String rolePrefix = "role:";
    private ShopHashStrategy strategy;

    public ShopAuthImpl(Vertx vertx, JDBCClient client) {
        this.client = client;
        this.strategy = new DefaultHashStrategy(vertx);
    }

    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String username = authInfo.getString("loginName");
        if (username == null) {
            resultHandler.handle(Future.failedFuture("authInfo must contain username in 'loginName' field"));
        } else {
            String password = authInfo.getString("password");
            if (password == null) {
                resultHandler.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
            } else {
                this.executeQuery(this.authenticateQuery, new JsonArray().add(username).add(username).add(username).add(Integer.valueOf(IsDeleted.NO.getValue())),
                        resultHandler, rs -> {
                            switch (rs.getNumRows()) {
                                case 0: {
                                    resultHandler.handle(Future.succeededFuture());
                                    break;
                                }
                                case 1: {
                                    JsonArray row = rs.getResults().get(0);
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
                                        resultHandler.handle(Future.succeededFuture(new ShopUser(this.strategy.getUserId(row).longValue(), username, this, this.rolePrefix)));
                                        break;
                                    }
                                    resultHandler.handle(Future.succeededFuture());
                                    break;
                                }
                                default: {
                                    resultHandler.handle(Future.failedFuture("Failure in authentication"));
                                }
                            }
                        }
                );
            }
        }
    }

    public ShopAuth setAuthenticationQuery(String authenticationQuery) {
        this.authenticateQuery = authenticationQuery;
        return this;
    }

    public ShopAuth setRolesQuery(String rolesQuery) {
        this.rolesQuery = rolesQuery;
        return this;
    }

    public ShopAuth setPermissionsQuery(String permissionsQuery) {
        this.permissionsQuery = permissionsQuery;
        return this;
    }

    public ShopAuth setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
        return this;
    }

    public ShopAuth setHashStrategy(ShopHashStrategy strategy) {
        this.strategy = strategy;
        return this;
    }

    <T> void executeQuery(String query, JsonArray params, Handler<AsyncResult<T>> resultHandler, Consumer<ResultSet> resultSetConsumer) {
        this.client.getConnection(res -> {
                    if (res.succeeded()) {
                        SQLConnection conn = res.result();
                        conn.queryWithParams(query, params, queryRes -> {
                                    if (queryRes.succeeded()) {
                                        ResultSet rs = queryRes.result();
                                        resultSetConsumer.accept(rs);
                                    } else {
                                        resultHandler.handle(Future.failedFuture(queryRes.cause()));
                                    }
                                    conn.close(closeRes -> {
                                            }
                                    );
                                }
                        );
                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }
                }
        );
    }

    public String computeHash(String password, String salt, int version) {
        return this.strategy.computeHash(password, salt, version);
    }

    public String generateSalt() {
        return this.strategy.generateSalt();
    }

    public ShopAuth setNonces(JsonArray nonces) {
        this.strategy.setNonces(nonces.getList());
        return this;
    }

    String getRolesQuery() {
        return this.rolesQuery;
    }

    String getPermissionsQuery() {
        return this.permissionsQuery;
    }
}