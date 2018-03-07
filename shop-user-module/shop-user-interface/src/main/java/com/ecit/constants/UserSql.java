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
    public static final String GET_USER_BY_ID = "select * from t_user where user_id = ? and is_deleted = ?";
}
