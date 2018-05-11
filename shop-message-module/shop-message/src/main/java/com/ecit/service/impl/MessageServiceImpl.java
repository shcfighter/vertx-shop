package com.ecit.service.impl;

import com.ecit.common.enmu.RegisterType;
import com.ecit.service.IMessageService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.reactivex.core.Future;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.mail.MailClient;
import io.vertx.reactivex.ext.mongo.MongoClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by za-wangshenhua on 2018/2/28.
 */
public class MessageServiceImpl implements IMessageService{

    private static final Logger LOGGER = LogManager.getLogger(MessageServiceImpl.class);
    private static MongoClient mongoClient;
    private static MailClient mailClient = null;
    private static String fromUser;

    public MessageServiceImpl(Vertx vertx, JsonObject config) {
        mongoClient = MongoClient.createShared(vertx, config.getJsonObject("mongodb"));
        JsonObject mail = config.getJsonObject("mail");
        mailClient = MailClient.createShared(vertx, new MailConfig(mail));
        fromUser = mail.getString("fromUser");
    }

    @Override
    public IMessageService saveMessage(String destination, RegisterType type, Handler<AsyncResult<String>> resultHandler) {
        final String code = (int)((Math.random()*9+1)*100000) + "";
        JsonObject document = new JsonObject()
                .put("type", type.name())
                .put("destination", destination)
                .put("code", code)
                .put("status", 0)
                .put("createTime", new Date().getTime());

        Future<String> future = Future.future();
        //mongoClient.rxInsert(MONGODB_COLLECTION, document).subscribe(future::complete, future::fail);
        mongoClient.insert(MONGODB_COLLECTION, document, handler -> {
            if(handler.succeeded()){
                future.complete(code);
            } else {
                LOGGER.error("发送目标【{}】验证码【{}】保存失败", destination, code, handler.cause());
                future.fail(handler.cause());
            }
        });
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IMessageService findMessage(String destination, RegisterType type, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject query = new JsonObject()
                .put("type", type.name())
                .put("destination", destination)
                .put("status", 0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 5);
        query.put("createTime", new JsonObject().put("$gte", calendar.getTime().getTime()));
        Future<JsonObject> future = Future.future();
        mongoClient.rxFindOne(MONGODB_COLLECTION, query, null).subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IMessageService updateMessage(String destination, RegisterType type, Handler<AsyncResult<MongoClientUpdateResult>> resultHandler) {
        JsonObject query = new JsonObject()
                .put("type", type.name())
                .put("destination", destination);
        JsonObject update = new JsonObject()
                .put("$set", new JsonObject()
                        .put("status", 1)
                        .put("updateTime", new Date().getTime()));
        Future<MongoClientUpdateResult> future = Future.future();
        mongoClient.rxUpdateCollectionWithOptions(MONGODB_COLLECTION, query, update, new UpdateOptions()
                .setMulti(true))
                .subscribe(future::complete, future::fail);
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IMessageService registerEmailMessage(String destination, Handler<AsyncResult<String>> resultHandler) {
        Future future = Future.future();
        this.saveMessage(destination, RegisterType.email, handler -> {
            if(handler.succeeded()){
                final String code = handler.result();
                MailMessage message = new MailMessage();
                message.setFrom(fromUser);
                message.setTo(destination);
                message.setText("注册激活");
                message.setHtml("<a href=\"http://111.231.132.168/api/user/activate/" + destination + "/" + code + "\">验证码：" + code + "</a>");
                mailClient.sendMail(message, result -> {
                    if (result.succeeded()) {
                        LOGGER.info("邮件【{}】发送成功！", destination);
                        future.complete(handler.result());
                    } else {
                        LOGGER.error("邮件【{}】发送失败", destination, result.cause());
                        future.fail(result.cause());
                    }
                });
            } else {
                LOGGER.info("生成邮件验证code失败！", handler.cause());
                future.fail(handler.cause());
            }
        });
        future.setHandler(resultHandler);
        return this;
    }

    @Override
    public IMessageService registerMobileMessage(String destination, Handler<AsyncResult<String>> resultHandler) {
        Future future = Future.future();
        this.saveMessage(destination, RegisterType.mobile, handler -> {
            if(handler.succeeded()){
                final String code = handler.result();
                LOGGER.info("短信【{}】发送成功！", destination);
                //todo 调用短信发送平台
                future.complete(code);


            } else {
                LOGGER.info("生成验证code失败！", handler.cause());
                future.fail(handler.cause());
            }
        });
        future.setHandler(resultHandler);
        return this;
    }
}
