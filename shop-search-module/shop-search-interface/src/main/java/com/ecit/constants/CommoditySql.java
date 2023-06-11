package com.ecit.constants;

/**
 * Created by shwang on 2018/3/19.
 */
public interface CommoditySql {

    /**
     * 通过id查询商品详情
     */
    static final String FIND_COMMODITY_BY_ID = """
                select commodity_id::text, commodity_name, brand_id, brand_name, category_id, category_name, price::numeric, original_price::numeric, 
                        num, freeze_num, status, image_url, freight::numeric, is_deleted, create_time, update_time, description, remarks, versions 
                        from t_commodity where commodity_id = $1 and is_deleted = 0 and status = 1
            """;

    /**
     * 通过ids查询商品详情
     */
    static final String FIND_COMMODITY_BY_IDS = """
                select commodity_id::text, commodity_name, brand_id, brand_name, category_id, category_name, price::numeric, original_price::numeric, 
                num, freeze_num, status, image_url, freight::numeric, is_deleted, create_time, update_time, description, remarks, versions 
                from t_commodity where commodity_id in({{ids}}) and is_deleted = 0 and status = 1
            """;
    /**
     * 商品下单扣库存
     */
    static final String ORDER_COMMODITY_BY_ID = """
                update t_commodity set num = (num - $1), freeze_num = (freeze_num + $2), versions = (versions + 1) where commodity_id = $3 and is_deleted = 0
            """;

    /**
     *  扣库存预处理
     */
    static final String ORDER_LOG_SQL = """
                insert into t_order_commodity_log(log_id, order_id, commodity_id, ip, num, logistics, pay_way, create_time) 
                values($1, $2, $3, $4, $5, $6, $7, now())
            """;
}
