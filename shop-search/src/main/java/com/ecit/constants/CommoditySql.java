package com.ecit.constants;

/**
 * Created by za-wangshenhua on 2018/3/19.
 */
public class CommoditySql {

    /**
     * 通过id查询商品详情
     */
    public static final String FIND_COMMODITY_BY_ID = "select * from t_commodity where commodity_id = ? and is_deleted = 0 and status = 1;";
}
