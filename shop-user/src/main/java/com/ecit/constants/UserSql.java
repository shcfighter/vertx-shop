package com.ecit.constants;

/**
 * Created by za-wangshenhua on 2018/2/5.
 */
public interface UserSql {

    /**
     * 手机号码注册
     */
    public static final String REGISTER_SQL = "insert into t_user " +
            "(user_id, password, status, is_lock, mobile, email, salt,  create_time, update_time) " +
            "values(?, ?, ?, ?, ?, ?, ?, now(), now())";

    public static final String LOGIN_SQL = "select user_id, password pwd from t_user where status = ? and is_lock = ? " +
            "and (mobile = ? or email = ? or login_name = ?)";
}
