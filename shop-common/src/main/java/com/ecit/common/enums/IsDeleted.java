package com.ecit.common.enums;

/**
 * Created by shwang on 2018/2/23.
 */
public enum IsDeleted {
    /**
     * 已删除  1
     */
    YES(1),
    /**
     * 未删除  0
     */
    NO(0);
    private int value;

    public int getValue() {
        return value;
    }

    IsDeleted(int value) {
        this.value = value;
    }
}
