package com.ecit.enmu;

/**
 * 付款方式
 */
public enum PayType {
    ACCOUNT(1, "余额支付"),
    ALIPAY(2, "支付宝"),
    WCHAT(3, "微信"),
    CARD(4, "银行卡");

    private int key;
    private String desc;

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    PayType(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}
