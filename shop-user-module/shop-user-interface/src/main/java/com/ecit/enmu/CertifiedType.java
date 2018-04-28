package com.ecit.enmu;

/**
 * 用户认证类型
 */
public enum CertifiedType {
    /**
     * 1-登录密码认证；2-支付密码认证；3-手机验证认证；4-邮箱验证认证；5-实名认证；6-安全问题认证
     */
    LOGIN_CERTIFIED(1, "登录密码认证"),
    PAY_CERTIFIED(2, "支付密码认证"),
    MOBILE_CERTIFIED(3, "手机验证认证"),
    EMAIL_CERTIFIED(4, "邮箱验证认证"),
    REALNAME_CERTIFIED(5, "实名认证"),
    QUESTION_CERTIFIED(6, "安全问题认证");

    int key;
    String value;

    CertifiedType() {
    }

    CertifiedType(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
