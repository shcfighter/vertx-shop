package com.ecit.constants;

/**
 * Created by za-wangshenhua on 2018/2/5.
 */
public interface UserSql {

    /**
     * 手机号码注册
     */
    public static final String REGISTER_SQL = "insert into t_user " +
            "(user_id, login_name, password, status, is_deleted, mobile, email, salt,  create_time, update_time) " +
            "values(?, ?, ?, ?, ?, ?, ?, ?, now(), now())";

    /**
     * 登录
     */
    public static final String LOGIN_SQL = "select user_id userId, login_name loginName, password pwd, salt, status from t_user where" +
            " (mobile = ? or email = ? or login_name = ?) and is_deleted = ?";

    /**
     * 修改密码
     */
    public static final String CHANGE_PWD_SQL = "update t_user set password = ?, versions = ? where user_id = ? and versions = ?";

    /**
     * 通过id获取用户信息
     */
    public static final String GET_USER_BY_ID_SQL = "select * from t_user where user_id = ? and is_deleted = ?";

    /**
     * 查询激活账户
     */
    public static final String SELECT_ACTIVATE_EMAIL_USER_SQL = "select * from t_user where email = ? and status = 0 and is_deleted = 0";

    /**
     *  查询激活账户
     */
    public static final String ACTIVATE_EMAIL_USER_SELECT_SQL = "select * from t_user where email = ? and status = 0 and is_deleted = 0";

    /**
     * 激活账号
     */
    public static final String ACTIVATE_EMAIL_USER_SQL = "update t_user set status = 1, update_time = now(), versions = versions + 1 where user_id = ? and versions = ?";

    /**
     * 查询用户信息
     */
    public static final String GET_USER_INFO_SQL = "select u.user_id, u.login_name, u.user_name, u.mobile, u.email, ui.user_info_id, ui.sex, ui.birthday, ui.photo_url, " +
            "u.versions, ui.versions info_versions from t_user u left join t_user_info ui on(u.user_id = ui.user_id) where u.user_id = ? and u.is_deleted = 0";

    /**
     * 修改用户信息
     */
    public static final String UPDATE_USER_SQL = "update t_user set login_name = ?, user_name = ?, mobile = ?, email = ?, " +
            "versions = versions + 1, update_time = now() where user_id = ? and versions = ?";

    public static final String INSERT_USER_INFO_SQL = "insert into t_user_info(user_info_id, user_id, real_name, sex, birthday, photo_url, is_deleted, create_time) " +
            "values (?, ?, ?, ?, ?, ?, 0, now())";

    public static final String UPDATE_USER_INFO_SQL = "update t_user_info set real_name = ?, sex = ?, birthday = ?, photo_url = ?, " +
            "versions = versions + 1, update_time = now() where user_id = ? and versions = ?";
}
