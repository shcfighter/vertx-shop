//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ecit.common.utils.salt;

import io.vertx.core.json.JsonArray;

import java.util.List;

public interface ShopHashStrategy {
    String generateSalt();

    String computeHash(String var1, String var2, int var3);

    String getHashedStoredPwd(JsonArray var1);

    String getSalt(JsonArray var1);

    Long getUserId(JsonArray var1);

    String getLoginName(JsonArray var1);

    void setNonces(List<String> var1);
}
