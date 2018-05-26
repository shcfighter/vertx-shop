package com.ecit.constants;

public interface AccountSql {

    /**
     * 创建用户资金帐户
     */
    static final String INSERT_ACCOUNT_SQL = "insert into t_account(account_id, user_id, create_time) values (?, ?, now())";

    /**
     * 根据用户id查询资金账户
     */
    static final String FIND_ACCOUNT_BY_USERID_SQL = "select account_id, amount::numeric, status, versions from t_account where is_deleted = 0 and user_id = ?";
}
