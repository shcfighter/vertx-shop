package com.ecit.handler;

import com.ecit.common.id.IdWorker;

/**
 * Created by shwang on 2018/2/5.
 */
public class IdBuilder {

    /**
     * 创建唯一生成器
     */
    private static final IdWorker ID_WORKER = new IdWorker(2, 1);

    /**
     * 获取唯一id
     * @return
     */
    public static long getUniqueId(){
        return ID_WORKER.nextId();
    }
}
