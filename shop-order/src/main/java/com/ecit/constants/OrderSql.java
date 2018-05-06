package com.ecit.constants;

public interface OrderSql {
    /**
     * 下单
     */
    static final String INSERT_ORDER_SQL = "insert into t_order(order_id, user_id, shipping_information_id, order_status, leave_message, order_details, total_price, freight, create_time)" +
            " values (?, ?, ?, ?, ?, ?, ?, ?, now());";

    /**
     * 分页查询订单详情（订单状态）
     */
    static final String FIND_PAGE_ORDER_SQL = "select order_id::text, shipping_information_id, create_time, order_status, cancel_time, send_time, order_details, total_price, freight from t_order " +
            "where is_deleted = 0 and user_id = ? and order_status = ? order by create_time desc limit ? offset ?";

    /**
     * 分页查询订单详情(所有订单)
     */
    static final String FIND_ALL_PAGE_ORDER_SQL = "select order_id::text, shipping_information_id, create_time, order_status, cancel_time, send_time, order_details, total_price, freight from t_order " +
            "where is_deleted = 0 and user_id = ? order by create_time desc limit ? offset ?";

    /**
     * 查询订单详情数量（订单状态）
     */
    static final String FIND_ORDER_ROWNUM_SQL = "select count(1) rowNum from t_order where is_deleted = 0 and user_id = ? and order_status = ?";

    /**
     * 查询订单详情数量(所有订单)
     */
    static final String FIND_ALL_ORDER_ROWNUM_SQL = "select count(1) rowNum from t_order where is_deleted = 0 and user_id = ?";

    /**
     *  根据订单id查询订单详情
     */
    static final String find_order_by_id = "select order_id::text, user_id, shipping_information_id, create_time, order_details, " +
            "total_price, order_status, cancel_time, send_time, freight, is_deleted, remarks, versions, leave_message" +
            " from t_order where order_id = ?";

    /**
     * 订单发货
     */
    static final String ship_order_sql = "update t_order send_time = now(), order_status = ?, versions = (versions + 1) where order_id = ? and versions = ?";

    /**
     * 取消订单
     */
    static final String cancel_order_sql = "update t_order cancel_time = now(), order_status = ?, versions = (versions + 1) where order_id = ? and versions = ?";


}