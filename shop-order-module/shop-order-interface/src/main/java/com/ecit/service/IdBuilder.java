package com.ecit.service;

import com.ecit.common.id.IdWorker;

/**
 * Created by za-wangshenhua on 2018/2/5.
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
