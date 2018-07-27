package com.ecit.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shwang on 2018/2/2.
 */
@Setter
@Getter
@NoArgsConstructor
@DataObject(generateConverter = true)
public class User implements Serializable{
    private long userId;
    private String userName;
    private String loginName;
    private String password;
    private int status;
    private int isLock;
    private String email;
    private String mobile;
    private Date createTime;
    private Date updateTime;
    private String remarks;
    private int versions;

    public User(JsonObject jsonObject) {
        UserConverter.fromJson(jsonObject, this);
    }

    public User jsonToObject(JsonObject jsonObject){
        return jsonObject.mapTo(User.class);
    }

    public JsonObject objectToJson(){
        return JsonObject.mapFrom(this);
    }
}
