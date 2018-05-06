package com.ecit.enmu;

public enum UserSex {
    /**
     *   2-男;
     */
    MALE(2, "男"),
    /**
     *  1-女；
     */
    FEMALE(1, "女"),
    /**
     *  0-保密；
     */
    CONFIDENTIALITY(0, "保密");

    private int key;
    private String value;

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    UserSex() {
    }

    UserSex(int key, String value) {
        this.key = key;
        this.value = value;
    }
}
