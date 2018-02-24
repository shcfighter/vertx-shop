//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ecit.gateway.auth;

import com.ecit.gateway.auth.impl.ShopAuthImpl;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.jdbc.JDBCClient;

@VertxGen
public interface ShopAuth extends AuthProvider {
    String DEFAULT_AUTHENTICATE_QUERY = "SELECT PASSWORD, PASSWORD_SALT FROM USER WHERE USERNAME = ?";
    String DEFAULT_ROLES_QUERY = "SELECT ROLE FROM USER_ROLES WHERE USERNAME = ?";
    String DEFAULT_PERMISSIONS_QUERY = "SELECT PERM FROM ROLES_PERMS RP, USER_ROLES UR WHERE UR.USERNAME = ? AND UR.ROLE = RP.ROLE";
    String DEFAULT_ROLE_PREFIX = "role:";

    static ShopAuth create(Vertx vertx, JDBCClient client) {
        return new ShopAuthImpl(vertx, client);
    }

    @Fluent
    ShopAuth setAuthenticationQuery(String var1);

    @Fluent
    ShopAuth setRolesQuery(String var1);

    @Fluent
    ShopAuth setPermissionsQuery(String var1);

    @Fluent
    ShopAuth setRolePrefix(String var1);

    @GenIgnore
    ShopAuth setHashStrategy(ShopHashStrategy var1);

    default String computeHash(String password, String salt) {
        return this.computeHash(password, salt, -1);
    }

    String computeHash(String var1, String var2, int var3);

    String generateSalt();

    @Fluent
    ShopAuth setNonces(JsonArray var1);
}
