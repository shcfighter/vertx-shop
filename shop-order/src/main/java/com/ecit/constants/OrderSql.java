package com.ecit.constants;

public interface OrderSql {
    /**
     * 下单
     */
    static final String INSERT_ORDER_SQL = "insert into t_order(order_id, user_id, shipping_information_id, order_status, leave_message, order_details, create_time)" +
            " values (?, ?, ?, ?, ?, ?, now());";

    /**
     * 分页查询订单详情（订单状态）
     */
    static final String FIND_PAGE_ORDER_SQL = "select order_id, shipping_information_id, create_time, order_status, cancel_time, send_time, order_details from t_order " +
            "where is_deleted = 0 and user_id = ? and order_status = ? order by create_time desc limit ? offset ?";

    /**
     * 分页查询订单详情(所有订单)
     */
    static final String FIND_ALL_PAGE_ORDER_SQL = "select order_id, shipping_information_id, create_time, order_status, cancel_time, send_time, order_details from t_order " +
            "where is_deleted = 0 and user_id = ? order by create_time desc limit ? offset ?";

    /**
     *  根据订单id查询订单详情
     */
    static final String find_order_by_id = "select * from t_order where order_id = ?";

    /**
     * 订单发货
     */
    static final String ship_order_sql = "update t_order send_time = now(), order_status = ?, versions = (versions + 1) where order_id = ? and versions = ?";

    /**
     * 取消订单
     */
    static final String cancel_order_sql = "update t_order cancel_time = now(), order_status = ?, versions = (versions + 1) where order_id = ? and versions = ?";


}
