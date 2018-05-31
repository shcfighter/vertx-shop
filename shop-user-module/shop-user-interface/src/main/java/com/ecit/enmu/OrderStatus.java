package com.ecit.enmu;

/**
 * 订单状态
 */
public enum OrderStatus {
    effective(1, "有效"),
    cancel(2, "已取消订单"),
    Refund(3, "已退款"),
    Ship(4, "已发货"),
    invalid(0, "订单失效");

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
