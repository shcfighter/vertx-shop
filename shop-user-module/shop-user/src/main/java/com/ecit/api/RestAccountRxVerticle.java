package com.ecit.api;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.constants.Constants;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.handler.IAccountHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by shwang on 2018/2/2.
 */
public class RestAccountRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestAccountRxVerticle.class);
    private static final String HTTP_ACCOUNT_SERVICE = "http_account_service_api";
    private IAccountHandler accountHandler;

    @Override
    public void start() throws Exception {
        super.start();
        this.accountHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IAccountHandler.ACCOUNT_SERVICE_ADDRESS).build(IAccountHandler.class);
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler

        /**
         * 登录拦截
         */
        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        router.get("/getAccount").handler(this::getAccountHandler);
        router.post("/payOrder/:orderId").handler(this::payOrderHandler);
        router.put("/setPayPwd").handler(this::setPayPwdHandler);

        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("account.http.address", "localhost");
        int port = config().getInteger("account.http.port", 8087);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_ACCOUNT_SERVICE, host, port, "account.api.name").subscribe();
            LOGGER.info("shop-account server started!");
        }, error -> {
            LOGGER.info("shop-account server start fail!", error);
        });
    }

    /**
     * 获取账户金额
     * @param context
     */
    private void getAccountHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        accountHandler.findAccountHandler(token, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "获取账户金额失败!");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询账户金额成功", handler.result());
            return ;
        });
    }

    private void payOrderHandler(RoutingContext context){
        final String token = context.request().getHeader(Constants.TOKEN);
        JsonObject params = context.getBodyAsJson();
        accountHandler.payOrderHandler(token, Long.parseLong(context.pathParam("orderId")), params, handler -> {
            if(handler.failed()){
                LOGGER.error("支付订单失败", handler.cause());
                String resultMessage = handler.cause().getMessage();
                if(StringUtils.isEmpty(resultMessage)){
                    resultMessage = "支付订单失败！";
                }
                this.returnWithFailureMessage(context, resultMessage);
                return ;
            }
            this.returnWithSuccessMessage(context, "支付订单成功", handler.result());
            return ;
        });
    }

    /**
     * 设置支付密码
     * @param context
     */
    private void setPayPwdHandler(RoutingContext context){
        JsonObject params = context.getBodyAsJson();
        String payPwd = params.getString("pay_pwd");
        String confirmPwd = params.getString("confirm_pwd");
        if(!StringUtils.equals(payPwd, confirmPwd)){
            this.returnWithFailureMessage(context, "两次密码不一致！");
            return ;
        }
        final String token = context.request().getHeader(Constants.TOKEN);
        accountHandler.changePayPwdHandler(token, params, handler -> {
            if(handler.failed()){
                LOGGER.error("设置支付密码失败！", handler.cause());
                this.returnWithFailureMessage(context, "设置支付密码失败！");
                return ;
            }
            this.returnWithSuccessMessage(context, "设置支付密码成功", handler.result());
            return ;
        });
    }
}
