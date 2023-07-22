package com.ecit.constants;

public interface OrderSql {
    /**
     * 下单
     */
    static final String INSERT_ORDER_SQL = """
                insert into t_order(order_id, user_id, shipping_information_id, order_status, leave_message, order_details, total_price, freight, create_time) 
                 values ($1, $2, $3, $4, $5, $6, $7, $8, now()) returning order_id;
            """;

    /**
     * 分页查询订单详情（订单状态）
     */
    static final String FIND_PAGE_ORDER_SQL = """
                select order_id::text, shipping_information_id, create_time, order_status, cancel_time, send_time, order_details, total_price, freight from t_order 
                where is_deleted = 0 and user_id = $1 and order_status = $2 order by create_time desc limit $3 offset $4
            """;

    /**
     * 分页查询订单详情(所有订单)
     */
    static final String FIND_ALL_PAGE_ORDER_SQL = """
                select order_id::text, shipping_information_id, create_time, order_status, cancel_time, send_time, order_details, total_price, freight from t_order 
                where is_deleted = 0 and user_id = $1 order by create_time desc limit $2 offset $3
            """;

    /**
     * 查询订单详情数量（订单状态）
     */
    static final String FIND_ORDER_ROWNUM_SQL = "select count(1) rowNum from t_order where is_deleted = 0 and user_id = $1 and order_status = $2";

    /**
     * 查询订单详情数量(所有订单)
     */
    static final String FIND_ALL_ORDER_ROWNUM_SQL = "select count(1) rowNum from t_order where is_deleted = 0 and user_id = $1";

    /**
     *  根据订单id查询订单详情
     */
    static final String FIND_ORDER_BY_ID = """
            select order_id::text, user_id, shipping_information_id, create_time, order_details, 
            total_price::numeric, order_status, cancel_time, send_time, freight::numeric, is_deleted, remarks, versions, leave_message
             from t_order where order_id = $1 and user_id = $2
             """;

    /**
     * 订单付款成功
     */
    static final String PAY_ORDER_SQL = "update t_order set order_status = $1, versions = (versions + 1) where order_id = $2 and versions = $3";

    /**
     * 订单发货
     */
    static final String SHIP_ORDER_SQL = "update t_order send_time = now(), order_status = $1, versions = (versions + 1) where order_id = $2 and versions = $3";

    /**
     * 取消订单
     */
    static final String CANCEL_ORDER_SQL = "update t_order set cancel_time = now(), order_status = $1, versions = (versions + 1) where order_id = $2 and versions = $3";

    /**
     * 取消退款
     */
    static final String CANCEL_REFUND_SQL = "update t_order set cancel_time = null, order_status = $1, versions = (versions + 1) where order_id = $2 and versions = $3";

    /**
     *  退货
     */
    static final String REFUND_SQL = """
            insert into t_order_refund(refund_id, order_id, refund_type, refund_reason, refund_money, refund_description, user_id, create_time) 
            values($1, $2, $3, $4, $5, $6, $7, now())
        """;

    /**
     * 删除退货单
     */
    static final String DELETE_REFUND_SQL = "update t_order_refund set is_deleted = 1, update_time = now(), versions = (versions + 1) where order_id = $1 and is_deleted = 0";


}
