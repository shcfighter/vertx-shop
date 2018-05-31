package com.ecit.api;

import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.service.IAccountService;
import com.ecit.service.IAddressService;
import com.ecit.service.ICertifiedService;
import com.ecit.service.IUserService;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by za-wangshenhua on 2018/2/2.
 */
public class RestAccountRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LogManager.getLogger(RestAccountRxVerticle.class);
    private static final String HTTP_ACCOUNT_SERVICE = "http_account_service_api";
    private final IAccountService accountService;

    public RestAccountRxVerticle(IAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());
        // API route handler
        router.get("/getAccount").handler(context -> this.requireLogin(context, this::getAccountHandler));
        router.post("/payOrder/:orderId").handler(context -> this.requireLogin(context, this::payOrderHandler));

        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("account.http.address", "localhost");
        int port = config().getInteger("account.http.port", 8087);

        // create HTTP server and publish REST service
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
     * @param principal
     */
    private void getAccountHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        accountService.findAccount(userId, handler -> {
            if(handler.failed()){
                LOGGER.error("获取用户信息失败", handler.cause());
                this.returnWithFailureMessage(context, "获取账户金额失败!");
                return ;
            }
            this.returnWithSuccessMessage(context, "查询账户金额成功", handler.result());

        });
    }

    private void payOrderHandler(RoutingContext context, JsonObject principal){
        final Long userId = principal.getLong("userId");
        JsonObject params = context.getBodyAsJson();
        accountService.payOrder(userId, Long.parseLong(context.pathParam("orderId")), params.getString("pay_pwd"),
                handler -> {
            if(handler.failed()){
                LOGGER.error("支付订单失败", handler.cause());
                this.returnWithFailureMessage(context, handler.cause().getMessage());
                return ;
            }
            this.returnWithSuccessMessage(context, "支付订单成功", handler.result());

        });
    }
}
