package com.ecit.enums;

/**
 * 订单状态
 */
public enum OrderStatus {
    /**
     * 1=有效
     */
    VALID(1, "有效"),
    /**
     * 2=已取消订单
     */
    PAY(2, "已取消订单"),
    /**
     * 3=已退款
     */
    REFUND(3, "已退款"),
    /**
     * 4=已发货
     */
    SHIP(4, "已发货"),
    /**
     * 0=订单失效
     */
    INVALID(0, "订单失效"),
    /**
     * -1=已取消订单
     */
    CANCEL(-1, "已取消订单");

    private int value;
    private String desc;

    OrderStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
