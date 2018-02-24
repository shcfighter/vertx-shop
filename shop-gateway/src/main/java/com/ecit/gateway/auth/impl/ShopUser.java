//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ecit.gateway.auth.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class ShopUser extends AbstractUser {
    private ShopAuthImpl authProvider;
    private Long userId;
    private String loginName;
    private JsonObject principal;
    private String rolePrefix;

    public ShopUser() {
    }

    public ShopUser(long userId, String loginName, ShopAuthImpl authProvider, String rolePrefix) {
        this.userId = userId;
        this.loginName = loginName;
        this.authProvider = authProvider;
        this.rolePrefix = rolePrefix;
    }

    public void doIsPermitted(String permissionOrRole, Handler<AsyncResult<Boolean>> resultHandler) {
        if(permissionOrRole != null && permissionOrRole.startsWith(this.rolePrefix)) {
            this.hasRoleOrPermission(permissionOrRole.substring(this.rolePrefix.length()), this.authProvider.getRolesQuery(), resultHandler);
        } else {
            this.hasRoleOrPermission(permissionOrRole, this.authProvider.getPermissionsQuery(), resultHandler);
        }

    }

    public JsonObject principal() {
        if(this.principal == null) {
            this.principal = (new JsonObject()).put("loginName", this.loginName)
                    .put("userId", this.userId);
        }

        return this.principal;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        if(authProvider instanceof ShopAuthImpl) {
            this.authProvider = (ShopAuthImpl)authProvider;
        } else {
            throw new IllegalArgumentException("Not a JDBCAuthImpl");
        }
    }

    public void writeToBuffer(Buffer buff) {
        super.writeToBuffer(buff);
        byte[] bytes = String.valueOf(this.userId).getBytes(StandardCharsets.UTF_8);
        buff.appendInt(bytes.length);
        buff.appendBytes(bytes);
        bytes = this.loginName.getBytes(StandardCharsets.UTF_8);
        buff.appendInt(bytes.length);
        buff.appendBytes(bytes);
        bytes = this.rolePrefix.getBytes(StandardCharsets.UTF_8);
        buff.appendInt(bytes.length);
        buff.appendBytes(bytes);
    }

    public int readFromBuffer(int pos, Buffer buffer) {
        pos = super.readFromBuffer(pos, buffer);
        int len = buffer.getInt(pos);
        pos += 4;
        byte[] bytes = buffer.getBytes(pos, pos + len);
        this.userId = Long.parseLong(new String(bytes, StandardCharsets.UTF_8));
        pos += len;
        len = buffer.getInt(pos);
        pos += 4;
        bytes = buffer.getBytes(pos, pos + len);
        this.loginName = new String(bytes, StandardCharsets.UTF_8);
        pos += len;
        len = buffer.getInt(pos);
        pos += 4;
        bytes = buffer.getBytes(pos, pos + len);
        this.rolePrefix = new String(bytes, StandardCharsets.UTF_8);
        pos += len;
        return pos;
    }

    private void hasRoleOrPermission(String roleOrPermission, String query, Handler<AsyncResult<Boolean>> resultHandler) {
        this.authProvider.executeQuery(query, (new JsonArray()).add(this.loginName), resultHandler, (rs) -> {
            boolean has = false;
            Iterator var4 = rs.getResults().iterator();

            while(var4.hasNext()) {
                JsonArray result = (JsonArray)var4.next();
                String theRoleOrPermission = result.getString(0);
                if(roleOrPermission.equals(theRoleOrPermission)) {
                    resultHandler.handle(Future.succeededFuture(Boolean.valueOf(true)));
                    has = true;
                    break;
                }
            }

            if(!has) {
                resultHandler.handle(Future.succeededFuture(Boolean.valueOf(false)));
            }

        });
    }
}
