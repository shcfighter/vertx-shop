//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ecit.gateway.auth.impl;

import com.ecit.common.enmu.IsDeleted;
import com.ecit.constants.UserSql;
import com.ecit.gateway.auth.ShopAuth;
import com.ecit.gateway.auth.ShopHashStrategy;
import io.vertx.core.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.PRNG;
import io.vertx.ext.auth.User;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class ShopAuthImpl implements AuthProvider, ShopAuth {
    private JDBCClient client;
    private String authenticateQuery = UserSql.LOGIN_SQL;
    private String rolesQuery = "SELECT ROLE FROM USER_ROLES WHERE USERNAME = ?";
    private String permissionsQuery = "SELECT PERM FROM ROLES_PERMS RP, USER_ROLES UR WHERE UR.USERNAME = ? AND UR.ROLE = RP.ROLE";
    private String rolePrefix = "role:";
    private ShopHashStrategy strategy;

    public ShopAuthImpl(Vertx vertx, JDBCClient client) {
        this.client = client;
        this.strategy = new ShopAuthImpl.DefaultHashStrategy(vertx);
    }

    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String username = authInfo.getString("loginName");
        if(username == null) {
            resultHandler.handle(Future.failedFuture("authInfo must contain username in 'loginName' field"));
        } else {
            String password = authInfo.getString("password");
            if(password == null) {
                resultHandler.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
            } else {
                this.executeQuery(this.authenticateQuery, (new JsonArray()).add(username).add(username).add(username).add(IsDeleted.NO.getValue()), resultHandler, (rs) -> {
                    switch(rs.getNumRows()) {
                    case 0:
                        resultHandler.handle(Future.succeededFuture(null));
                        break;
                    case 1:
                        JsonArray row = (JsonArray)rs.getResults().get(0);
                        String hashedStoredPwd = this.strategy.getHashedStoredPwd(row);
                        String salt = this.strategy.getSalt(row);
                        int version = -1;
                        int sep = hashedStoredPwd.lastIndexOf(36);
                        if(sep != -1) {
                            try {
                                version = Integer.parseInt(hashedStoredPwd.substring(sep + 1));
                            } catch (NumberFormatException var11) {
                                resultHandler.handle(Future.failedFuture("Invalid nonce version: " + version));
                                return;
                            }
                        }

                        String hashedPassword = this.strategy.computeHash(password, salt, version);
                        if(hashedStoredPwd.equals(hashedPassword)) {
                            resultHandler.handle(Future.succeededFuture(new ShopUser(this.strategy.getUserId(row), username, this, this.rolePrefix)));
                        } else {
                            resultHandler.handle(Future.succeededFuture(null));
                        }
                        break;
                    default:
                        resultHandler.handle(Future.failedFuture("Failure in authentication"));
                    }

                });
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
        this.client.getConnection((res) -> {
            if(res.succeeded()) {
                SQLConnection conn = (SQLConnection)res.result();
                conn.queryWithParams(query, params, (queryRes) -> {
                    if(queryRes.succeeded()) {
                        ResultSet rs = (ResultSet)queryRes.result();
                        resultSetConsumer.accept(rs);
                    } else {
                        resultHandler.handle(Future.failedFuture(queryRes.cause()));
                    }

                    conn.close((closeRes) -> {
                    });
                });
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }

        });
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

    private class DefaultHashStrategy implements ShopHashStrategy {
        private final PRNG random;
        private List<String> nonces;
        private final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

        DefaultHashStrategy(Vertx vertx) {
            this.random = new PRNG(vertx);
        }

        public String generateSalt() {
            byte[] salt = new byte[32];
            this.random.nextBytes(salt);
            return this.bytesToHex(salt);
        }

        public String computeHash(String password, String salt, int version) {
            try {
                String concat = (salt == null?"":salt) + password;
                if(version >= 0) {
                    if(this.nonces == null) {
                        throw new VertxException("nonces are not available");
                    }

                    if(version < this.nonces.size()) {
                        concat = concat + (String)this.nonces.get(version);
                    }
                }

                MessageDigest md = MessageDigest.getInstance("SHA-512");
                byte[] bHash = md.digest(concat.getBytes(StandardCharsets.UTF_8));
                return version >= 0?this.bytesToHex(bHash) + '$' + version:this.bytesToHex(bHash);
            } catch (NoSuchAlgorithmException var7) {
                throw new VertxException(var7);
            }
        }

        public String getHashedStoredPwd(JsonArray row) {
            return row.getString(2);
        }

        public String getSalt(JsonArray row) {
            return row.getString(3);
        }

        @Override
        public Long getUserId(JsonArray row) {
            return row.getLong(0);
        }

        @Override
        public String getLoginName(JsonArray row) {
            return row.getString(1);
        }

        public void setNonces(List<String> nonces) {
            this.nonces = Collections.unmodifiableList(nonces);
        }

        private String bytesToHex(byte[] bytes) {
            char[] chars = new char[bytes.length * 2];

            for(int i = 0; i < bytes.length; ++i) {
                int x = 255 & bytes[i];
                chars[i * 2] = this.HEX_CHARS[x >>> 4];
                chars[1 + i * 2] = this.HEX_CHARS[15 & x];
            }

            return new String(chars);
        }
    }
}
