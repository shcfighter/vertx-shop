package com.ecit.api;

import com.ecit.common.auth.ShopUserSessionHandler;
import com.ecit.common.enums.RegisterType;
import com.ecit.common.rx.RestAPIRxVerticle;
import com.ecit.handler.IMessageHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shwang on 2018/2/2.
 */
public class RestMessageRxVerticle extends RestAPIRxVerticle{

    private static final Logger LOGGER = LoggerFactory.getLogger(RestMessageRxVerticle.class);
    private static final String HTTP_MESSAGE_SERVICE = "http_message_service_api";
    private IMessageHandler messageHandler;

    @Override
    public void start() throws Exception {
        super.start();
        this.messageHandler = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).build(IMessageHandler.class);
        final Router router = Router.router(vertx);
        // body handler
        router.route().handler(BodyHandler.create());

        /**
         * 登录拦截
         */
        router.getDelegate().route().handler(ShopUserSessionHandler.create(vertx.getDelegate(), this.config()));

        // API route handler
        router.post("/insertMessage").handler(this::insertMessageHandler);
        router.post("/sendEmailMessage").handler(this::sendEmailMessageHandler);
        router.post("/sendMobileMessage").handler(this::sendMobileMessageHandler);
        //全局异常处理
        this.globalVerticle(router);

        // get HTTP host and port from configuration, or use default value
        String host = config().getString("message.http.address", "localhost");
        int port = config().getInteger("message.http.port", 8083);

        // create HTTP server and publish REST handler
        createHttpServer(router, host, port).subscribe(server -> {
            this.publishHttpEndpoint(HTTP_MESSAGE_SERVICE, host, port, "message.api.name").subscribe();
            LOGGER.info("shop-message server started!");
        }, error -> {
            LOGGER.info("shop-message server start fail!", error);
        });
    }

    /**
     *  发送邮件验证码
     * @param context
     */
    private void sendEmailMessageHandler(RoutingContext context){
        messageHandler.registerEmailMessage(context.getBodyAsJson().getString("destination"), handler ->{
            if(handler.succeeded()){
                LOGGER.info("邮箱发送成功，code:{}", handler.result());
                this.returnWithSuccessMessage(context, "邮箱发送成功");
            } else {
                LOGGER.error("邮箱发送失败", handler.cause());
                this.returnWithFailureMessage(context, "邮箱发送失败");
            }
        });
    }

    /**
     *  发送手机验证码
     * @param context
     */
    private void sendMobileMessageHandler(RoutingContext context){
        messageHandler.registerMobileMessage(context.getBodyAsJson().getString("destination"), handler ->{
            if(handler.succeeded()){
                LOGGER.info("手机验证码发送成功，code:{}", handler.result());
                this.returnWithSuccessMessage(context, "手机验证码发送成功");
            } else {
                LOGGER.error("手机验证码发送失败", handler.cause());
                this.returnWithFailureMessage(context, "手机验证码发送失败");
            }
        });
    }

    /**
     *
     * @param context
     */
    private void insertMessageHandler(RoutingContext context){
        messageHandler.saveMessage(context.getBodyAsJson().getString("destination"), RegisterType.email, handler ->{
            if(handler.succeeded()){
                LOGGER.info("插入成功，code:{}", handler.result());
                this.returnWithSuccessMessage(context, "插入成功");
            } else {
                LOGGER.error("插入失败", handler.cause());
                this.returnWithFailureMessage(context, "插入失败");
            }
        });
    }

    /*private void findMessageHandler(RoutingContext context){
        final String destination = context.getBodyAsJson().getString("destination");
        messageHandler.findMessage(destination, RegisterType.mobile, handler ->{
            if(handler.succeeded()){
                messageService.updateMessage(destination, RegisterType.mobile, deleteHandler -> {
                    if (deleteHandler.succeeded()) {
                        LOGGER.info("数据删除成功！");
                    } else {
                        LOGGER.info("数据删除失败！", deleteHandler.cause());
                    }
                });
                this.Ok(context, ResultItems.getReturnItemsSuccess(0, handler.result()));
                //this.returnWithSuccessMessage(context, "插入成功");
            } else {
                LOGGER.info("查询失败", handler.cause());
                this.returnWithFailureMessage(context, "查询失败");
            }
        });
    }*/

}
