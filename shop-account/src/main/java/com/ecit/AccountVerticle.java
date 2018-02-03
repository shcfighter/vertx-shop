package com.ecit;

import com.ecit.common.rx.BaseMicroserviceRxVerticle;
import com.ecit.service.IAccountService;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class AccountVerticle extends BaseMicroserviceRxVerticle{

    private IAccountService accountService;
    @Override
    public void start() throws Exception {
        super.start();


    }
}
