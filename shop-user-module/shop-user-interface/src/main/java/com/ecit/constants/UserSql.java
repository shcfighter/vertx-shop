package com.ecit.constants;

/**
 * Created by shwang on 2018/2/5.
 */
public interface UserSql {

    /**
     * 手机号码注册
     */
    static final String REGISTER_SQL = """
                insert into t_user 
                (user_id, login_name, password, status, is_deleted, mobile, email, salt,  create_time, update_time) 
                values($1, $2, $3, $4, $5, $6, $7, $8, now(), now())
            """;

    /**
     * 登录
     */
    static final String LOGIN_SQL = """
                select user_id::text userId, login_name loginName, password pwd, salt, status from t_user where 
                (mobile = $1 or email = $2 or login_name = $3) and is_deleted = $4
            """;

    /**
     * 修改密码
     */
    static final String CHANGE_PWD_SQL = "update t_user set password = $1, versions = $2 where user_id = $3 and versions = $4";

    /**
     * 通过id获取用户信息
     */
    static final String GET_USER_BY_ID_SQL = "select * from t_user where user_id = $1 and is_deleted = $2";

    /**
     *  查询激活账户
     */
    static final String ACTIVATE_EMAIL_USER_SELECT_SQL = "select * from t_user where email = $1 and status = 0 and is_deleted = 0";

    /**
     * 激活账号
     */
    static final String ACTIVATE_EMAIL_USER_SQL = "update t_user set status = 1, update_time = now(), versions = versions + 1 where user_id = $1 and versions = $2";

    /**
     * 查询用户信息
     */
    static final String GET_USER_INFO_SQL = """
                select u.user_id::text, u.login_name, u.user_name, u.mobile, u.email, ui.user_info_id, ui.sex, ui.birthday, ui.photo_url, 
                u.versions, ui.versions info_versions from t_user u left join t_user_info ui on(u.user_id = ui.user_id) where u.user_id = $1 and u.is_deleted = 0
            """;

    /**
     * 修改用户信息
     */
    static final String UPDATE_USER_SQL = """
                update t_user set login_name = $1, user_name = $2, mobile = $3, email = $4, 
                versions = versions + 1, update_time = now() where user_id = $5 and versions = $6
            """;

    /**
     * 修改email
     */
    static final String UPDATE_USER_EMAIL_SQL = """
                update t_user set email = $1, 
                versions = versions + 1, update_time = now() where user_id = $2 and versions = $3
            """;

    /**
     * 修改mobile
     */
    static final String UPDATE_USER_MOBILE_SQL = """
                update t_user set mobile = $1, 
                versions = versions + 1, update_time = now() where user_id = $2 and versions = $3
            """;

    /**
     *  保存用户详情信息
     */
    static final String INSERT_USER_INFO_SQL = """
                insert into t_user_info(user_info_id, user_id, real_name, sex, birthday, photo_url, is_deleted, create_time) 
                values ($1, $2, $3, $4, $5, $6, 0, now())
            """;

    /**
     *  更新用户详情信息
     */
    static final String UPDATE_USER_INFO_SQL = """
                update t_user_info set real_name = $1, sex = $2, birthday = $3, photo_url = $4, 
                versions = versions + 1, update_time = now() where user_id = $5 and versions = $6
            """;

    /**
     * 查询认证信息
     */
    static final String GET_USER_IDCARD_INFO_SQL = """
                select user_info_id, id_card, real_name, id_card_positive_url, id_card_negative_url from t_user_info  
                where user_id = $1 and is_deleted = 0
            """;

    /**
     *  实名认证
     */
    static final String UPDATE_USER_IDCARD_SQL = """
                update t_user_info set real_name = $1, id_card = $2, id_card_positive_url = $3, id_card_negative_url = $4, 
                versions = versions + 1, update_time = now() where user_id = $5 and versions = $6
            """;

    /**
     * 实名认证时插入
     */
    static final String INSERT_USER_INFO_IDCARD_SQL = """
                insert into t_user_info(user_info_id, user_id, real_name, id_card, id_card_positive_url, id_card_negative_url, is_deleted, create_time) 
                values ($1, $2, $3, $4, $5, $6, 0, now())
            """;

    /**
     * 通过userid查询用户详情信息
     */
    static final String SELECT_USER_INFO_BY_USERID_SQL = "select * from t_user_info where user_id = $1 and is_deleted = 0";

    /**
     *  保存用户认证信息
     */
    static final String INSERT_USER_CERTIFIED_SQL = "insert into t_user_certified(certified_id, user_id, certified_type, certified_time, remarks) values ($1, $2, $3, $4, $5)";

    /**
     *  更新用户认证信息
     */
    static final String UPDATE_USER_CERTIFIED_SQL = "update t_user_certified set update_time = $1, remarks = $2, versions = versions + 1 where certified_id = $3";

    /**
     *  查询用户认证信息
     */
    static final String SELECT_USER_CERTIFIED_SQL = """
                select certified_id::text, user_id::text, certified_type, certified_time, update_time, is_deleted, remarks, versions 
                from t_user_certified where user_id = $1 and is_deleted = 0
            """;

    /**
     * 根据类型查询用户认证信息
     */
    static final String SELECT_USER_CERTIFIED_BY_TYPE_SQL = """
                select certified_id, user_id, certified_type, certified_time, update_time, is_deleted, remarks, versions 
                from t_user_certified where user_id = $1 and certified_type = $2 and is_deleted = 0
            """;

    /**
     *  新增收货地址
     */
    static final String INSERT_ADDRESS_SQL = """
                insert into t_shipping_information(shipping_information_id, user_id, receiver, mobile, province_code, city_code, county_code, address, address_details, create_time) 
                values ($1, $2, $3, $4, $5, $6, $7, $8, $9, now())
            """;

    /**
     *  查询收货地址列表
     */
    static final String FIND_ADDRESS_SQL = """
                select shipping_information_id::text, user_id::text, receiver, mobile, province_code, city_code, county_code, 
                address, address_details, is_default, versions from t_shipping_information where user_id = $1 and is_deleted = 0 order by is_default desc, create_time desc 
            """;

    /**
     *  通过id修改收货地址
     */
    static final String UPDATE_ADDRESS_BY_ID_SQL = """
                update t_shipping_information receiver = $1, mobile = $2, province_code = $3, city_code = $4, 
                county_code = $5, address = $6, address_details = $7, versions = versions + 1, update_time = now() where shipping_information_id = $8
            """;

    /**
     * 设置为默认收货地址
     */
    static final String UPDATE_ADDRESS_BY_DEFAULT_SQL = "update t_shipping_information set is_default = 1, versions = versions + 1, update_time = now() where shipping_information_id = $1 ";

    /**
     *  设置为非默认收货地址
     */
    static final String UPDATE_ADDRESS_BY_NOT_DEFAULT_SQL = "update t_shipping_information set is_default = 0, versions = versions + 1, update_time = now() where user_id = $1 and is_default = 1";

    /**
     *  删除收货地址
     */
    static final String DELETE_ADDRESS_BY_ID_SQL = "update t_shipping_information set is_deleted = 1, versions = versions + 1, update_time = now() where shipping_information_id = $1 ";

    /**
     * 根据id查询收货地址
     */
    static final String GET_ADDRESS_BY_ID_SQL = """
                select shipping_information_id, user_id, receiver, mobile, province_code, city_code, county_code, 
                address, address_details, versions from t_shipping_information where shipping_information_id = $1 
            """;

    /**
     * 绑定手机号
     */
    static final String BIND_MOBILE_SQL = "update t_user set mobile = $1, versions = versions + 1, update_time = now() where user_id = $2 and versions = $3";

}