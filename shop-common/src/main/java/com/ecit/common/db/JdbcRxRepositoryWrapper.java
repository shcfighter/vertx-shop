package com.ecit.common.db;

import com.ecit.common.constants.Constants;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Promise;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.RedisConnection;
import io.vertx.reactivex.redis.client.Response;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.redis.client.RedisOptions;
import io.vertx.sqlclient.PoolOptions;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper and wrapper class for JDBC repository services.
 */
public class JdbcRxRepositoryWrapper {

  private static final Logger LOGGER = LogManager.getLogger(JdbcRxRepositoryWrapper.class);

  private static final int MAX_RECONNECT_RETRIES = 16;

  private final RedisOptions options = new RedisOptions().addConnectionString("redis://127.0.0.1:6379");
  private final AtomicBoolean CONNECTING = new AtomicBoolean();
  protected RedisConnection redisConnection;
  protected RedisAPI redisClient;
  protected final PgPool pgPool;

  public JdbcRxRepositoryWrapper(Vertx vertx, JsonObject config) {
    JsonObject postgresqlConfig = config.getJsonObject("postgresql", new JsonObject());
 /*   this.postgreSQLClient = PostgreSQLClient.createShared(vertx, postgresqlConfig);
    JsonObject redisConfig = config.getJsonObject("redis", new JsonObject());
    this.redisClient = RedisClient.create(vertx, new RedisOptions().setHost(redisConfig.getString("host", "localhost"))
            .setPort(redisConfig.getInteger("port", 6379)).setAuth(redisConfig.getString("auth")));
*/
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

    createRedisClient(vertx)
            .onSuccess(conn -> {
              // connected to redis!
              this.redisConnection = conn;
              redisClient = RedisAPI.api(redisConnection);
            }).onFailure(err -> {
              LOGGER.info("redis ");
            });
  }

  /**
   * Suitable for `add`, `exists` operation.
   *
   * @param params        query params
   * @param sql           sql
   */
  protected Single<Integer> executeNoResult(Tuple params, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute(params)
                    .map(rows -> rows.size()).doAfterTerminate(conn::close));
  }

  protected Single<Integer> execute(Tuple params, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute(params)
                    .map(rows -> rows.size()).doAfterTerminate(conn::close));
  }

  protected Single<JsonObject> retrieveOne(Tuple params, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute(params).map(rs -> {
              if (rs == null || 0 == rs.size()) {
                return new JsonObject();
              } else {
                List<JsonObject> resList = new ArrayList<>();
                rs.forEach(row -> {
                  resList.add(row.toJson());
                });
                return resList.get(0);
              }
            }).doAfterTerminate(conn::close));

  }

  protected int calcPage(int page, int size) {
    if (page <= 0)
      return 0;
    return size * (page - 1);
  }

  protected Single<List<JsonObject>> retrieveByPage(Tuple params, int size, int page, String sql) {
    params.addInteger(size).addInteger(calcPage(page, size));
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute(params)
                    .map(rs -> {
                      if (rs == null || 0 == rs.size()) {
                        return new ArrayList<JsonObject>();
                      } else {
                        List<JsonObject> resList = new ArrayList<>();
                        rs.forEach(row -> {
                          resList.add(row.toJson());
                        });
                        return resList;
                      }
                    }).doAfterTerminate(conn::close));
  }

  protected Single<List<JsonObject>> retrieveMany(Tuple params, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute(params)
                    .map(rs -> {
                      if (rs == null || 0 == rs.size()) {
                        return new ArrayList<JsonObject>();
                      } else {
                        List<JsonObject> resList = new ArrayList<>();
                        rs.forEach(row -> {
                          resList.add(row.toJson());
                        });
                        return resList;
                      }
                    }).doAfterTerminate(conn::close));
  }

  protected Single<List<JsonObject>> retrieveAll(String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute()
                    .map(rs -> {
                      if (rs == null || 0 == rs.size()) {
                        return new ArrayList<JsonObject>();
                      } else {
                        List<JsonObject> resList = new ArrayList<>();
                        rs.forEach(row -> {
                          resList.add(row.toJson());
                        });
                        return resList;
                      }
                    }).doAfterTerminate(conn::close));
  }

  protected Single<Integer> removeOne(Object id, String sql) {
    Tuple params = Tuple.of(id);
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute(params)
                    .map(rows -> rows.size()).doAfterTerminate(conn::close));
  }

  protected Single<Integer> removeAll(String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.preparedQuery(sql).rxExecute()
                    .map(rows -> rows.size()).doAfterTerminate(conn::close));
  }

  protected Single<SqlConnection> getConnection() {
    return pgPool.rxGetConnection();
  }

  /*protected Maybe<RowSet<Row>> withTransaction(Function<SqlConnection, Maybe<RowSet<Row>>> function) {
    *//*pool.rxWithTransaction((Function<SqlConnection, Maybe<RowSet<Row>>>) client -> client
                    // Create table
                    .query("select * from user").rxExecute()
                    // Insert colors
                    .flatMap(r -> client
                            .preparedQuery("INSERT INTO colors (name) VALUES (?)")
                            .rxExecuteBatch(Arrays.asList(Tuple.of("BLACK"), Tuple.of("PURPLE"))))
                    // Get colors if all succeeded
                    .flatMap(r -> client.query("SELECT * FROM colors").rxExecute())
                    .toMaybe())// Subscribe to get the final result
            .subscribe(rowSet -> {
              System.out.println("Results:");
              rowSet.forEach(row -> {
                System.out.println(row.toJson());
              });
            }, Throwable::printStackTrace);
  };*//*
    return pool.rxWithTransaction(function);
  }*/

  /*protected Single<UpdateResult> executeTransaction(List<JsonObject> arrays){
    *//*this.withTransaction((Function<SqlConnection, Maybe<RowSet<Row>>>) client -> {
      for (JsonObject json : arrays) {
        if (!json.containsKey("type")) {
          continue;
        }
        switch (json.getString("type")) {
          case "execute": {
            result = client.flatMap(updateResult -> conn.rxExecute(json.getString("sql")));
          }
          case "update": {
            result = result.flatMap(updateResult -> conn.rxUpdateWithParams(json.getString("sql"),
                    json.getJsonArray("params")));
          }
          default: {
          }
        }
      }
    })*//*
    return this.getConnection()
            .flatMap(conn -> {
              Single result = conn
                      // Disable auto commit to handle transaction manually
                      .rxSetAutoCommit(false)
                      // Switch from Completable to default Single value
                      .toSingleDefault(false);
              for (JsonObject json : arrays) {
                if (!json.containsKey("type")) {
                  continue;
                }
                switch (json.getString("type")) {
                  case "execute": {
                    result = result.flatMap(updateResult -> conn.rxExecute(json.getString("sql")));
                  }
                  case "update": {
                    result = result.flatMap(updateResult -> conn.rxUpdateWithParams(json.getString("sql"),
                            json.getJsonArray("params")));
                  }
                  default: {
                  }
                }
              }
              // commit if all succeeded
              Single<UpdateResult> resultSingle = result.flatMap(updateResult -> conn.rxCommit().toSingleDefault(true).map(commit -> updateResult));
              // Rollback if any failed with exception propagation
              resultSingle = resultSingle.onErrorResumeNext(ex -> conn.rxRollback()
                      .toSingleDefault(true)
                      .onErrorResumeNext(ex2 -> Single.error(new CompositeException(ex, ex2)))
                      .flatMap(ignore -> Single.error(ex))
              )
                      // close the connection regardless succeeded or failed
                      .doAfterTerminate(conn::close);
              return resultSingle;
            });
  }*/

  /*protected Single executeTransaction(JsonObject... arrays){
    return this.executeTransaction(Arrays.asList(arrays));
  }*/

  /**
   * 缓存获取token
   * @param token
   * @return
   */
  protected Future<JsonObject> getSession(String token){
    if(StringUtils.isEmpty(token)){
      return Future.failedFuture("token empty!");
    }
    Promise<JsonObject> redisResult = Promise.promise();
    redisClient.hget(Constants.VERTX_WEB_SESSION, token, re -> {
      if (re.failed()) {
        LOGGER.error("redis hget : ", re.cause());
        redisResult.fail(re.cause());
      } else {
        Response response = re.result();
        String user = response.toString();
        if(StringUtils.isEmpty(user)){
          redisResult.fail("user session empty!");
          return ;
        }
        redisResult.complete(new JsonObject(user));
      }
    });
    return redisResult.future();
  }

  /**
   * token添加缓存
   * @param token
   * @param jsonObject
   */
  protected void setSession(String token, JsonObject jsonObject){
    redisClient.rxHset(Arrays.asList(Constants.VERTX_WEB_SESSION, token, jsonObject.toString())).subscribe();
    redisClient.rxExpire(List.of(Constants.VERTX_WEB_SESSION, String.valueOf(Constants.SESSION_EXPIRE_TIME))).subscribe();
  }

  /**
   * Will create a redis client and setup a reconnect handler when there is
   * an exception in the connection.
   */
  private Future<RedisConnection> createRedisClient(Vertx vertx) {
    Promise<RedisConnection> promise = Promise.promise();

    if (CONNECTING.compareAndSet(false, true)) {
      Redis.createClient(vertx, options)
              .rxConnect().subscribe(conn -> {
                // make sure to invalidate old connection if present
                if (this.redisConnection != null) {
                  this.redisConnection.close();
                }

                // make sure the client is reconnected on error
                conn.exceptionHandler(e -> {
                  // attempt to reconnect,
                  // if there is an unrecoverable error
                  attemptReconnect(vertx, 0);
                });

                // allow further processing
                promise.complete(conn);
                CONNECTING.set(false);
              }, t -> {
                promise.fail(t);
                CONNECTING.set(false);
              });
    } else {
      promise.complete();
    }

    return promise.future();
  }

  /**
   * Attempt to reconnect up to MAX_RECONNECT_RETRIES
   */
  private void attemptReconnect(Vertx vertx, int retry) {
    if (retry > MAX_RECONNECT_RETRIES) {
      // we should stop now, as there's nothing we can do.
      CONNECTING.set(false);
    } else {
      // retry with backoff up to 10240 ms
      long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);

      vertx.setTimer(backoff, timer -> {
        createRedisClient(vertx)
                .onFailure(t -> attemptReconnect(vertx, retry + 1));
      });
    }
  }
}
