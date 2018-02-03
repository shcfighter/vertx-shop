package com.ecit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
@Setter
@Getter
@NoArgsConstructor
public class User implements Serializable{
    private String userId;
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

}
