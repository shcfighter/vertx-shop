package com.ecit.handler.impl;

import com.ecit.common.db.JdbcRxRepositoryWrapper;
import com.ecit.common.enums.RegisterType;
import com.ecit.common.utils.JsonUtils;
import com.ecit.common.utils.salt.DefaultHashStrategy;
import com.ecit.common.utils.salt.ShopHashStrategy;
import com.ecit.constants.AccountSql;
import com.ecit.enmu.CertifiedType;
import com.ecit.enmu.PayStatus;
import com.ecit.enmu.PayType;
import com.ecit.enums.OrderStatus;
import com.ecit.handler.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.Objects;

public class AccountHandler extends JdbcRxRepositoryWrapper implements IAccountHandler {

    private static final Logger LOGGER = LogManager.getLogger(AccountHandler.class);
    static Vertx vertx;
    private static ShopHashStrategy hashStrategy;
    private IOrderHandler orderService;
    private ICertifiedHandler certifiedService;
    private IMessageHandler messageService;

    public AccountHandler(Vertx vertx, JsonObject config) {
        super(vertx, config);
        this.vertx = vertx;
        this.hashStrategy = new DefaultHashStrategy(vertx.getDelegate());
        this.orderService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IOrderHandler.ORDER_SERVICE_ADDRESS).build(IOrderHandler.class);
        this.certifiedService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(ICertifiedHandler.CERTIFIED_SERVICE_ADDRESS).build(ICertifiedHandler.class);
        this.messageService = new ServiceProxyBuilder(vertx.getDelegate()).setAddress(IMessageHandler.MESSAGE_SERVICE_ADDRESS).build(IMessageHandler.class);
    }

    /**
     * 初始化用户资金账户
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public IAccountHandler insertAccount(long userId, Handler<AsyncResult<Integer>> handler) {
        Promise<Integer> promise = Promise.promise();
        this.execute(Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(userId), AccountSql.INSERT_ACCOUNT_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 根据用户id查询资金账户
     * @param userId
     * @param handler
     * @return
     */
    @Override
    public IAccountHandler findAccount(long userId, Handler<AsyncResult<JsonObject>> handler) {
        Promise<JsonObject> promise = Promise.promise();
        this.retrieveOne(Tuple.tuple().addLong(userId), AccountSql.FIND_ACCOUNT_BY_USERID_SQL)
                .subscribe(promise::complete, promise::fail);
        promise.future().onComplete(handler);
        return this;
    }

    @Override
    public IAccountHandler findAccountHandler(String token, Handler<AsyncResult<JsonObject>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Promise<JsonObject> promise = Promise.promise();
        sessionFuture.andThen(sessionResult -> {
            JsonObject session = sessionResult.result();
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                promise.fail("can not get session");
            }
            this.retrieveOne(Tuple.tuple().addLong(session.getLong("userId")), AccountSql.FIND_ACCOUNT_BY_USERID_SQL)
                    .subscribe(promise::complete, promise::fail);

        });
        promise.future().onComplete(handler);
        return this;
    }

    /**
     * 订单支付（账户余额）
     * @param token
     * @param orderId
     * @param params
     * @param handler
     * @return
     */
    @Override
    public IAccountHandler payOrderHandler(String token, long orderId, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            //Future<JsonObject> orderFuture = Future.future();
            Promise<JsonObject> orderPromise = Promise.promise();
            orderService.getOrderById(orderId, userId, orderPromise);
            return orderPromise.future().compose(order -> {
                //检查库存；检查账户余额；扣款；
                if(Objects.isNull(order)){
                    LOGGER.error("用户【{}】支付订单【{}】不存在！", userId, orderId);
                    return Future.failedFuture("订单不存在！");
                }
                if(OrderStatus.VALID.getValue() != order.getInteger("order_status").intValue()){
                    LOGGER.error("订单【{}】状态【{}】不正确！", orderId, order.getInteger("order_status"));
                    return Future.failedFuture("订单状态不正确！");
                }
                Promise<JsonObject> accountPromise = Promise.promise();

                this.findAccount(userId, accountPromise);
                Future updateAccountFuture = accountPromise.future().compose(account -> {
                    if(Objects.isNull(account)){
                        LOGGER.error("账户信息不存在！");
                        return Future.failedFuture("账户信息异常！");
                    }
                    if(StringUtils.isEmpty(account.getString("pay_pwd"))){
                        LOGGER.error("未设置支付密码！");
                        return Future.failedFuture("not_pay_pwd");
                    }
                    //验证密码
                    final String pwd = this.hashStrategy.computeHash(params.getString("pay_pwd"), account.getInteger("account_id").toString(), -1);
                    if(!StringUtils.equals(account.getString("pay_pwd"), pwd)){
                        LOGGER.error("支付密码不正确！");
                        return Future.failedFuture("支付密码不正确！");
                    }

                    final String totalPrice = order.getString("total_price");
                    if(new BigDecimal(account.getString("amount")).compareTo(new BigDecimal(totalPrice)) < 0){
                        return Future.failedFuture("账户余额不足！");
                    }
                    Promise updatePromise = Promise.promise();
                    pgPool.rxGetConnection().flatMap(conn ->
                            conn.preparedQuery(AccountSql.LESS_ACCOUNT_SQL).rxExecute(
                                            Tuple.tuple().addString(totalPrice).addString(totalPrice).addLong(userId).addInteger(account.getInteger("versions")))
                                    .flatMap(updateResult -> conn.preparedQuery(AccountSql.INSERT_PAY_LOG_SQL).rxExecute(
                                            Tuple.tuple().addLong(IdBuilder.getUniqueId()).addLong(userId).addLong(orderId).addInteger(PayType.ACCOUNT.getKey())
                                                    .addString(totalPrice).addString(new BigDecimal(account.getString("amount")).subtract(new BigDecimal(totalPrice)).toString())
                                                    .addInteger(PayStatus.FINISHED.getKey())))
                                    .doAfterTerminate(conn::close)
                    ).subscribe(updatePromise::complete, updatePromise::fail);
                    return updatePromise.future();
                });
                return updateAccountFuture;
            }).compose(updateAccount -> {
                //更改订单状态
                JsonObject orderJson = orderPromise.future().result();
                Promise<Integer> promise = Promise.promise();
                orderService.payOrder(orderId, orderJson.getInteger("versions"), promise);
                return promise.future();
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }

    @Override
    public IAccountHandler changePayPwdHandler(String token, JsonObject params, Handler<AsyncResult<Integer>> handler) {
        IUserHandler userService = new ServiceProxyBuilder(vertx.getDelegate())
                .setAddress(IUserHandler.USER_SERVICE_ADDRESS).build(IUserHandler.class);
        Future<JsonObject> sessionFuture = this.getSession(token);
        Future<Integer> resultFuture = sessionFuture.compose(session -> {
            if (JsonUtils.isNull(session)) {
                LOGGER.info("无法获取session信息");
                return Future.failedFuture("can not get session");
            }
            final long userId = session.getLong("userId");
            Promise<JsonObject> userPromise = Promise.promise();
            userService.getMemberById(userId, userPromise);
            return userPromise.future().compose(user -> {
                if(Objects.isNull(user) || user.isEmpty()){
                    return Future.failedFuture("用户不存在！");
                }
                final String mobile = user.getString("mobile");
                Promise<JsonObject> messagePromise = Promise.promise();
                messageService.findMessage(mobile, RegisterType.mobile, messagePromise);
                return messagePromise.future().compose(message ->{
                    if(Objects.isNull(message) || message.isEmpty()){
                        return Future.failedFuture("手机验证码不存在！");
                    }
                    if (!StringUtils.equals(message.getString("code"), params.getString("code"))) {
                        return Future.failedFuture("手机验证码不正确！");
                    }
                    Promise<JsonObject> accountPromise = Promise.promise();
                    this.findAccount(userId, accountPromise);
                    return accountPromise.future();
                }).compose(account -> {
                    if(Objects.isNull(account) || account.isEmpty()){
                        return Future.failedFuture("账户【" + userId + "】不存在！");
                    }
                    final String pwd = this.hashStrategy.computeHash(params.getString("pay_pwd"), account.getInteger("account_id").toString(), -1);
                    Promise<Integer> promise = Promise.promise();
                    this.execute(Tuple.tuple().addString(pwd).addLong(userId).addInteger(account.getInteger("versions")), AccountSql.CHANGE_PAY_PWD_SQL)
                            .subscribe(promise::complete, promise::fail);
                    certifiedService.sendUserCertified(userId, CertifiedType.PAY_CERTIFIED.getKey(), mobile, certifiedHandler -> {});
                    messageService.updateMessage(mobile, RegisterType.mobile, messageHandler -> {});
                    return promise.future();
                });
            });
        });
        resultFuture.onComplete(handler);
        return this;
    }
}
