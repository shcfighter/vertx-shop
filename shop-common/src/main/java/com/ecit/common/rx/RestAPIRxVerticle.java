package com.ecit.common.rx;

import com.ecit.common.result.ResultItems;
import io.reactivex.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.reactivex.ext.web.handler.CorsHandler;
import io.vertx.reactivex.ext.web.handler.SessionHandler;
import io.vertx.reactivex.ext.web.sstore.ClusteredSessionStore;
import io.vertx.reactivex.ext.web.sstore.LocalSessionStore;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * An abstract base rx-fied verticle that provides
 * several helper methods for developing RESTful services.
 *
 * @author Eric Zhao
 */
public abstract class RestAPIRxVerticle extends BaseMicroserviceRxVerticle {

  private static final Logger LOGGER = LogManager.getLogger(RestAPIRxVerticle.class);
  /**
   * 特殊url无需登录可以正常返回
   */
  private static final String SPECIALURL = "findCartRowNum";
  protected Single<HttpServer> createHttpServer(Router router, String host, int port) {
    return vertx.createHttpServer()
      .requestHandler(router::accept)
      .rxListen(port, host);
  }

  protected void enableCorsSupport(Router router) {
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    router.route().handler(CorsHandler.create("*")
      .allowedHeaders(allowHeaders)
      .allowedMethod(HttpMethod.GET)
      .allowedMethod(HttpMethod.POST)
      .allowedMethod(HttpMethod.PUT)
      .allowedMethod(HttpMethod.DELETE)
      .allowedMethod(HttpMethod.PATCH)
      .allowedMethod(HttpMethod.OPTIONS)
    );
  }

  protected void enableLocalSession(Router router, String name) {
    Objects.requireNonNull(name);
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx, name)));
  }

  protected void enableClusteredSession(Router router, String name) {
    Objects.requireNonNull(name);
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(ClusteredSessionStore.create(vertx, name)));
  }

  protected void requireLogin(RoutingContext context, BiConsumer<RoutingContext, JsonObject> biHandler) {
      /*context.request().headers().add("user-principal", new JsonObject().put("loginName", "test")
              .put("userId", 217024117029867520L).encodePrettily());*/
    Optional<JsonObject> principal = Optional.ofNullable(context.request().getHeader("user-principal"))
      .map(JsonObject::new);
    if (principal.isPresent()) {
      biHandler.accept(context, principal.get());
    } else {
      if (StringUtils.contains(context.request().uri(), SPECIALURL)) {
        this.returnWithSuccessMessage(context, null, 0);
        return ;
      }
      LOGGER.info("未登录，无权访问！");
      this.noAuth(context);
    }
  }

  protected void noAuth(RoutingContext context) {
    context.response().setStatusCode(401)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure("no_auth")));
  }

  protected void badRequest(RoutingContext context, Throwable ex) {
    context.response().setStatusCode(400)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure(ex.getMessage())));
  }

  protected void notFound(RoutingContext context) {
    context.response().setStatusCode(404)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsSuccess("not_found")));
  }

  protected void internalError(RoutingContext context, Throwable ex) {
    context.response().setStatusCode(500)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure(ex.getMessage())));
  }

  protected void notImplemented(RoutingContext context) {

    context.response().setStatusCode(501)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure("not_implemented")));
  }

  protected void badGateway(Throwable ex, RoutingContext context) {
    context.response()
            .setStatusCode(502)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure("bad_gateway")));
  }

  protected void serviceUnavailable(RoutingContext context) {
    context.fail(503);
  }

  protected void serviceUnavailable(RoutingContext context, Throwable ex) {
    context.response().setStatusCode(503)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure(ex.getMessage())));
  }

  protected void serviceUnavailable(RoutingContext context, String cause) {
    context.response().setStatusCode(503)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getEncodePrettily(ResultItems.getReturnItemsFailure(cause)));
  }

  protected void returnWithSuccessMessage(RoutingContext context, String message) {
    this.Ok(context, ResultItems.getReturnItemsSuccess(message));
  }

  protected <T> void returnWithSuccessMessage(RoutingContext context, String message, T items) {
    this.Ok(context, ResultItems.getReturnItemsSuccess( 0, items, message));
  }

  protected <T> void returnWithSuccessMessage(RoutingContext context, String message, int total, T items) {
    this.Ok(context, ResultItems.getReturnItemsSuccess(total, items, message));
  }

  protected <T> void returnWithSuccessMessage(RoutingContext context, String message, int total, T items, int page) {
    this.Ok(context, ResultItems.getReturnItemsSuccess(total, items, message, page));
  }

  protected void returnWithFailureMessage(RoutingContext context, String message) {
    this.Ok(context, ResultItems.getReturnItemsFailure(message));
  }

  protected void Ok(RoutingContext context, ResultItems items) {
    context.response().setStatusCode(200)
            .putHeader("content-type", "application/json")
            .end(ResultItems.getJsonObject(items).encodePrettily());
  }

  /**
   * This method generates handler for async methods in REST APIs.
   */
  protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Handler<T> handler) {
    return res -> {
      if (res.succeeded()) {
        handler.handle(res.result());
      } else {
        LOGGER.error(res.cause());
        internalError(context, res.cause());
      }
    };
  }

  /**
   * This method generates handler for async methods in REST APIs.
   * Use the result directly and invoke `toString` as the response. The content type is JSON.
   */
  protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context) {
    return ar -> {
      if (ar.succeeded()) {
        T res = ar.result();
        this.Ok(context, ResultItems.getReturnItemsSuccess(1, res));
      } else {
        LOGGER.error(ar.cause());
        internalError(context, ar.cause());
      }
    };
  }

  /**
   * This method generates handler for async methods in REST APIs.
   * Use the result directly and use given {@code converter} to convert result to string
   * as the response. The content type is JSON.
   *
   * @param context   routing context instance
   * @param converter a converter that converts result to a string
   * @param <T>       result type
   * @return generated handler
   */
  protected <T> Handler<AsyncResult<T>> resultHandler(RoutingContext context, Function<T, String> converter) {
    return ar -> {
      if (ar.succeeded()) {
        T res = ar.result();
        if (res == null) {
          serviceUnavailable(context, "invalid_result");
        } else {
          context.response()
                  .putHeader("content-type", "application/json")
                  .end(converter.apply(res));
        }
      } else {
        LOGGER.error(ar.cause());
        internalError(context, ar.cause());
      }
    };
  }

  /**
   * This method generates handler for async methods in REST APIs.
   * The result requires non-empty. If empty, return <em>404 Not Found</em> status.
   * The content type is JSON.
   *
   * @param context routing context instance
   * @param <T>     result type
   * @return generated handler
   */
  protected <T> Handler<AsyncResult<T>> resultHandlerNonEmpty(RoutingContext context) {
    return ar -> {
      if (ar.succeeded()) {
        T res = ar.result();
        if (res == null) {
          notFound(context);
        } else {
          context.response()
                  .putHeader("content-type", "application/json")
                  .end(res.toString());
        }
      } else {
        LOGGER.error(ar.cause());
        internalError(context, ar.cause());
      }
    };
  }

  /**
   * This method generates handler for async methods in REST APIs.
   * The content type is originally raw text.
   *
   * @param context routing context instance
   * @param <T>     result type
   * @return generated handler
   */
  protected <T> Handler<AsyncResult<T>> rawResultHandler(RoutingContext context) {
    return ar -> {
      if (ar.succeeded()) {
        T res = ar.result();
        context.response()
                .end(res == null ? "" : res.toString());
      } else {
        LOGGER.error(ar.cause());
        internalError(context, ar.cause());
      }
    };
  }

  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, JsonObject result) {
    return resultVoidHandler(context, result, 200);
  }

  /**
   * This method generates handler for async methods in REST APIs.
   * The result is not needed. Only the state of the async result is required.
   *
   * @param context routing context instance
   * @param result  result content
   * @param status  status code
   * @return generated handler
   */
  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, JsonObject result, int status) {
    return ar -> {
      if (ar.succeeded()) {
        context.response()
                .setStatusCode(status == 0 ? 200 : status)
                .putHeader("content-type", "application/json")
                .end(result.encodePrettily());
      } else {
        LOGGER.error(ar.cause());
        internalError(context, ar.cause());
      }
    };
  }

  protected Handler<AsyncResult<Void>> resultVoidHandler(RoutingContext context, int status) {
    return ar -> {
      if (ar.succeeded()) {
        context.response()
                .setStatusCode(status == 0 ? 200 : status)
                .putHeader("content-type", "application/json")
                .end();
      } else {
        LOGGER.error(ar.cause());
        internalError(context, ar.cause());
      }
    };
  }

  /**
   * This method generates handler for async methods in REST DELETE APIs.
   * Return format in JSON (successful status = 204):
   * <code>
   * {"message": "delete_success"}
   * </code>
   *
   * @param context routing context instance
   * @return generated handler
   */
  protected Handler<AsyncResult<Void>> deleteResultHandler(RoutingContext context) {
    return res -> {
      if (res.succeeded()) {
        context.response().setStatusCode(204)
                .putHeader("content-type", "application/json")
                .end(new JsonObject().put("message", "delete_success").encodePrettily());
      } else {
        LOGGER.error(res.cause());
        internalError(context, res.cause());
      }
    };
  }

  protected void globalVerticle(Router router){
    router.route().last().handler(context -> {
      this.returnWithFailureMessage(context, "404");
    }).failureHandler(context -> {
      LOGGER.error("服务器异常", context.failure());
      this.returnWithFailureMessage(context, "服务器异常");
    });
  }

  protected String getHeader(RoutingContext context, String key) {
    return Optional.ofNullable(context.request().getHeader(key)).orElse(null);
  }
}
