package com.ecit.common.db;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.List;

/**
 * Helper and wrapper class for JDBC repository services.
 */
public class JdbcRepositoryWrapper {

  protected final SQLClient postgreSQLClient;

  public JdbcRepositoryWrapper(Vertx vertx, JsonObject config) {
    this.postgreSQLClient = PostgreSQLClient.createShared(vertx, config);
  }

  protected void execute(JsonArray params, String sql, Future<Integer> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.updateWithParams(sql, params, res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        future.complete(res.result().getUpdated());
        conn.close();
      });
    });
  }

  protected void retrieveOne(JsonArray params, String sql, Future<JsonObject> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.queryWithParams(sql, params, res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        List<JsonObject> resList = res.result().getRows();
        if (resList == null || resList.isEmpty()) {
          future.complete(new JsonObject());
        } else {
          future.complete(resList.get(0));
        }
        conn.close();
      });
    });
  }

  protected int calcPage(int page, int size) {
    if (page <= 0)
      return 0;
    return size * (page - 1);
  }

  protected void retrieveByPage(JsonArray params, int size, int page, String sql, Future<List<JsonObject>> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.queryWithParams(sql, params.add(size).add(calcPage(page, size)), res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        future.complete(res.result().getRows());
        conn.close();
      });
    });
  }

  protected void retrieveMany(JsonArray params, String sql, Future<List<JsonObject>> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.queryWithParams(sql, params, res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        future.complete(res.result().getRows());
        conn.close();
      });
    });
  }

  protected void retrieveAll(String sql, Future<List<JsonObject>> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.query(sql, res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        future.complete(res.result().getRows());
        conn.close();
      });
    });
  }

  protected void removeOne(Object id, String sql, Future<Integer> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.updateWithParams(sql, new JsonArray().add(id), res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        future.complete(res.result().getUpdated());
        conn.close();
      });
    });
  }

  protected void removeAll(String sql, Future<Integer> future) {
    postgreSQLClient.getConnection(connHandler -> {
      final SQLConnection conn = connHandler.result();
      conn.update(sql, res -> {
        if (res.failed()) {
          future.fail(res.cause());
        }
        future.complete(res.result().getUpdated());
        conn.close();
      });
    });
  }

}
