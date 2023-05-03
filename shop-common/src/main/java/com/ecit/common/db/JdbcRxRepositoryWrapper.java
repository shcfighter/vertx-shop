package com.ecit.common.db;

import com.ecit.common.constants.Constants;
import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.reactivex.core.Promise;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.asyncsql.PostgreSQLClient;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.sql.SQLClient;
import io.vertx.reactivex.ext.sql.SQLConnection;
import io.vertx.reactivex.pgclient.PgPool;
import io.vertx.reactivex.redis.RedisClient;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.reactivex.redis.client.RedisConnection;
import io.vertx.reactivex.sqlclient.SqlConnection;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.redis.RedisOptions;
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
  protected RedisConnection redisClient;
  protected final PgPool pool;

  public JdbcRxRepositoryWrapper(Vertx vertx, JsonObject config) {
    JsonObject postgresqlConfig = config.getJsonObject("postgresql", new JsonObject());
 /*   this.postgreSQLClient = PostgreSQLClient.createShared(vertx, postgresqlConfig);
    JsonObject redisConfig = config.getJsonObject("redis", new JsonObject());
    this.redisClient = RedisClient.create(vertx, new RedisOptions().setHost(redisConfig.getString("host", "localhost"))
            .setPort(redisConfig.getInteger("port", 6379)).setAuth(redisConfig.getString("auth")));
*/
    PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(5432)
            .setHost(config.getString("host"))
            .setDatabase(config.getString("database"))
            .setUser(config.getString("username"))
            .setPassword(config.getString("password"))
            .setReconnectAttempts(2)
            .setReconnectInterval(1000);;

    // Pool options
    PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5)
            .setShared(true);

    pool = PgPool.pool(vertx, connectOptions, poolOptions);

    createRedisClient()
            .onSuccess(conn -> {
              // connected to redis!
              this.redisClient = conn;

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

  protected Single<List<JsonObject>> retrieveByPage(JsonArray param, int size, int page, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQueryWithParams(sql, param.add(size).add(calcPage(page, size)))
                    .map(ResultSet::getRows).doAfterTerminate(conn::close));
  }

  protected Single<List<JsonObject>> retrieveMany(JsonArray param, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQueryWithParams(sql, param)
                    .map(ResultSet::getRows).doAfterTerminate(conn::close));
  }

  protected Single<List<JsonObject>> retrieveAll(String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxQuery(sql)
                    .map(ResultSet::getRows).doAfterTerminate(conn::close));
  }

  protected Single<Integer> removeOne(Object id, String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxUpdateWithParams(sql, new JsonArray().add(id))
                    .map(UpdateResult::getUpdated).doAfterTerminate(conn::close));
  }

  protected Single<Integer> removeAll(String sql) {
    return this.getConnection()
            .flatMap(conn -> conn.rxUpdate(sql).map(UpdateResult::getUpdated)
                    .doAfterTerminate(conn::close));
  }

  protected Single<SqlConnection> getConnection() {
    return pool.rxGetConnection();
  }

  protected Single<UpdateResult> executeTransaction(List<JsonObject> arrays){
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
  }

  protected Single executeTransaction(JsonObject... arrays){
    return this.executeTransaction(Arrays.asList(arrays));
  }

  /**
   * 缓存获取token
   * @param token
   * @return
   */
  protected Future<JsonObject> getSession(String token){
    if(StringUtils.isEmpty(token)){
      return Future.failedFuture("token empty!");
    }
    Future<JsonObject> redisResult = Future.future();
    redisClient.hget(Constants.VERTX_WEB_SESSION, token, re -> {
      if (re.failed()) {
        LOGGER.error("redis hget : ", re.cause());
        redisResult.fail(re.cause());
      } else {
        String user = re.result();
        if(StringUtils.isEmpty(user)){
          redisResult.fail("user session empty!");
          return ;
        }
        redisResult.complete(new JsonObject(user));
      }
    });
    return redisResult;
  }

  /**
   * token添加缓存
   * @param token
   * @param jsonObject
   */
  protected void setSession(String token, JsonObject jsonObject){
    redisClient.rxHset(Constants.VERTX_WEB_SESSION, token, jsonObject.toString()).subscribe();
    redisClient.rxExpire(Constants.VERTX_WEB_SESSION, Constants.SESSION_EXPIRE_TIME).subscribe();
  }

  /**
   * Will create a redis client and setup a reconnect handler when there is
   * an exception in the connection.
   */
  private Future<RedisConnection> createRedisClient() {
    Promise<RedisConnection> promise = Promise.promise();

    if (CONNECTING.compareAndSet(false, true)) {
      Redis.createClient(vertx, options)
              .rxConnect().subscribe(conn -> {
                // make sure to invalidate old connection if present
                if (client != null) {
                  client.close();
                }

                // make sure the client is reconnected on error
                conn.exceptionHandler(e -> {
                  // attempt to reconnect,
                  // if there is an unrecoverable error
                  attemptReconnect(0);
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
  private void attemptReconnect(int retry) {
    if (retry > MAX_RECONNECT_RETRIES) {
      // we should stop now, as there's nothing we can do.
      CONNECTING.set(false);
    } else {
      // retry with backoff up to 10240 ms
      long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);

      vertx.setTimer(backoff, timer -> {
        createRedisClient()
                .onFailure(t -> attemptReconnect(retry + 1));
      });
    }
  }
}
