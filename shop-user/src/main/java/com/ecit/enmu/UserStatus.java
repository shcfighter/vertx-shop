package com.ecit.enmu;

/**
 * Created by za-wangshenhua on 2018/2/5.
 */
public enum UserStatus {
    /**
     * 0=禁用
     */
    DISABLED(0, "禁用"),
    /**
     *1=激活
     */
    ACTIVATION(1, "激活"),
    /**
     *-1=删除
     */
    DELETED(-1, "删除");

    private int status;
    private String desc;

    UserStatus() {
    }

    UserStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
