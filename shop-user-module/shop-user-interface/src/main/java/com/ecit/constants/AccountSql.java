package com.ecit.constants;

public interface AccountSql {

    /**
     * 创建用户资金帐户
     */
    static final String INSERT_ACCOUNT_SQL = "insert into t_account(account_id, user_id, create_time) values ($1, $2, now())";

    /**
     * 根据用户id查询资金账户
     */
    static final String FIND_ACCOUNT_BY_USERID_SQL = "select account_id, amount::numeric, pay_pwd, status, versions from t_account where is_deleted = 0 and user_id = $1";

    /**
     *  扣款
     */
    static final String LESS_ACCOUNT_SQL = """
                update t_account set amount = (amount - $1), update_time = now(), versions = (versions + 1)
                where is_deleted = 0 and amount >= $2 and user_id = $3 and versions = $4
            """;

    /**
     * 保存资金变动记录
     */
    static final String INSERT_PAY_LOG_SQL = "insert into t_pay_log(log_id, user_id, business_id, type, money, balance, status, create_time) values ($1, $2, $3, $4, $5, $6, $7, now())";

    /**
     * 修改支付密码
     */
    static final String CHANGE_PAY_PWD_SQL = "update t_account set pay_pwd = $1, update_time = now(), versions = (versions + 1) where user_id = $2 and versions = $3";
}
