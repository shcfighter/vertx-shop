package com.ecit.enmu;

/**
 * 付款方式
 */
public enum PayStatus {
    READY(0, "准备支付"),
    FINISHED(1, "支付完成");

    private int key;
    private String desc;

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    PayStatus(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}
