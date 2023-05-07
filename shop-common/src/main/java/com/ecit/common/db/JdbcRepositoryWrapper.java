package com.ecit.common.db;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Helper and wrapper class for JDBC repository services.
 */
public class JdbcRepositoryWrapper {

  protected final PgPool pgPool;

  public JdbcRepositoryWrapper(Vertx vertx, JsonObject config) {
    //this.postgreSQLClient = PostgreSQLClient.createShared(vertx, config);
    JsonObject postgresqlConfig = config.getJsonObject("postgresql", new JsonObject());
    PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(5432)
            .setHost(postgresqlConfig.getString("host"))
            .setDatabase(postgresqlConfig.getString("database"))
            .setUser(postgresqlConfig.getString("username"))
            .setPassword(postgresqlConfig.getString("password"))
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);;

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5)
            .setShared(true);

    pgPool = PgPool.pool(vertx, connectOptions, poolOptions);
  }

  protected void execute(Tuple params, String sql, Promise<Integer> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      conn.preparedQuery(sql).execute(params).onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        } else {
          promise.complete(res.result().size());
        }
        conn.close();
      });
    });
  }

  protected void retrieveOne(Tuple params, String sql, Promise<JsonObject> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      conn.preparedQuery(sql).execute(params).onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        } else {
          RowSet<Row> rs = res.result();
          List<JsonObject> resList = new ArrayList<>();
          if (Objects.nonNull(rs) && rs.size() != 0) {
            rs.forEach(row -> {
              resList.add(row.toJson());
            });
          }
          if (resList == null || resList.isEmpty()) {
            promise.complete(new JsonObject());
          } else {
            promise.complete(resList.get(0));
          }
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

  protected void retrieveByPage(Tuple params, int size, int page, String sql, Promise<List<JsonObject>> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      params.addInteger(size).addInteger(calcPage(page, size));
      conn.preparedQuery(sql).execute(params).onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        } else {
          RowSet<Row> rs = res.result();
          List<JsonObject> resList = new ArrayList<>();
          if (Objects.nonNull(rs) && rs.size() != 0) {
            rs.forEach(row -> {
              resList.add(row.toJson());
            });
          }
          if (resList == null || resList.isEmpty()) {
            promise.complete(new ArrayList<>());
          } else {
            promise.complete(resList);
          }
        }
        conn.close();
      });
    });
  }

  protected void retrieveMany(Tuple params, String sql, Promise<List<JsonObject>> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      conn.preparedQuery(sql).execute(params).onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        } else {
          RowSet<Row> rs = res.result();
          List<JsonObject> resList = new ArrayList<>();
          if (Objects.nonNull(rs) && rs.size() != 0) {
            rs.forEach(row -> {
              resList.add(row.toJson());
            });
          }
          if (resList == null || resList.isEmpty()) {
            promise.complete(new ArrayList<>());
          } else {
            promise.complete(resList);
          }
        }
        conn.close();
      });
    });
  }

  protected void retrieveAll(String sql, Promise<List<JsonObject>> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      conn.preparedQuery(sql).execute().onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        } else {
          RowSet<Row> rs = res.result();
          List<JsonObject> resList = new ArrayList<>();
          if (Objects.nonNull(rs) && rs.size() != 0) {
            rs.forEach(row -> {
              resList.add(row.toJson());
            });
          }
          if (resList == null || resList.isEmpty()) {
            promise.complete(new ArrayList<>());
          } else {
            promise.complete(resList);
          }
        }
        conn.close();
      });
    });
  }

  protected void removeOne(Object id, String sql, Promise<Integer> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      conn.preparedQuery(sql).execute(Tuple.of(id)).onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        }
        promise.complete(res.result().size());
        conn.close();
      });
    });
  }

  protected void removeAll(String sql, Promise<Integer> promise) {
    pgPool.getConnection(connHandler -> {
      final SqlConnection conn = connHandler.result();
      conn.preparedQuery(sql).execute().onComplete(res -> {
        if (res.failed()) {
          promise.fail(res.cause());
        }
        promise.complete(res.result().size());
        conn.close();
      });
    });
  }

}
