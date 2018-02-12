package com.ecit.enmu;

/**
 * Created by za-wangshenhua on 2018/2/5.
 */
public enum UserLock {
    /**
     *0=未发布
     */
    UNPUBLISH(0, "未发布"),
    /**
     * 1=发布
     */
    PUBLISH(1, "发布");

    private int lock;
    private String desc;

    UserLock() {
    }

    UserLock(int lock, String desc) {
        this.lock = lock;
        this.desc = desc;
    }

    public int getLock() {
        return lock;
    }

    public String getDesc() {
        return desc;
    }
}
