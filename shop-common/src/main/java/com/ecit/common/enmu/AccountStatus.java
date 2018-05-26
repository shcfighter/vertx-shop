package com.ecit.common.enmu;

public enum AccountStatus {
    normal(0, "正常"),
    disabled(1, "禁用");
    private int key;
    private String desc;

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    AccountStatus(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}

