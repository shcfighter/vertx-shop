package com.ecit.enmu;

/**
 * 订单状态
 */
public enum OrderStatus {
    EFFECTIVE(1, "有效"),
    CANCEL(2, "已取消订单"),
    REFUND(3, "已退款"),
    SHIP(4, "已发货"),
    INVALID(0, "订单失效");

    private int key;
    private String desc;

    public int getKey() {
        return key;
    }

    public String getDesc() {
        return desc;
    }

    OrderStatus(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}
