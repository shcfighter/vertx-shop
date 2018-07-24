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
     * 2=已付款
     */
    PAY(2, "已付款"),
    /**
     * 3=已退款
     */
    REFUND(3, "已退款"),
    /**
     * 4=已发货
     */
    SHIP(4, "已发货"),
    /**
     * 0=订单完成
     */
    FINISHED(0, "订单完成"),
    /**
     * 6=评价
     */
    EVALUATION(6, "评价"),
    /**
     * 7=申请退款退货
     */
    AUDIT_REFUND(7, "申请退款退货"),
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
