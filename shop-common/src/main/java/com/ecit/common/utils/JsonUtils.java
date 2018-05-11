package com.ecit.common.utils;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

public class JsonUtils {

    /**
     * 判断JsonObject是否为空
     * @param jsonObject
     * @return
     */
    public static boolean isNull(JsonObject jsonObject){
        return Objects.isNull(jsonObject) || jsonObject.isEmpty() || jsonObject.size() == 0;
    }

    /**
     * 判断JsonArray是否为空
     * @param jsonArray
     * @return
     */
    public static boolean isNull(JsonArray jsonArray){
        return Objects.isNull(jsonArray) || jsonArray.isEmpty() || jsonArray.size() == 0;
    }
}
