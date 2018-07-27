package com.ecit.common.rx;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.Future;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.circuitbreaker.CircuitBreaker;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.JDBCDataSource;
import io.vertx.servicediscovery.types.MessageSource;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * This Rx-fied verticle provides support for handler discovery.
 *
 */
public class BaseMicroserviceRxVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(BaseMicroserviceRxVerticle.class);

  protected ServiceDiscovery discovery;
  protected CircuitBreaker circuitBreaker;
  protected Set<Record> registeredRecords = new ConcurrentHashSet<>();

  @Override
  public void start() throws Exception {
    discovery = ServiceDiscovery.create(vertx, new ServiceDiscoveryOptions().setBackendConfiguration(config()));
    JsonObject cbOptions = config().getJsonObject("circuit-breaker") != null ?
      config().getJsonObject("circuit-breaker") : new JsonObject();
    circuitBreaker = CircuitBreaker.create(cbOptions.getString("name", "circuit-breaker"), vertx,
      new CircuitBreakerOptions()
        .setMaxFailures(cbOptions.getInteger("maxFailures", 5))
        .setTimeout(cbOptions.getLong("timeout", 10000L))
        .setFallbackOnFailure(true)
        .setResetTimeout(cbOptions.getLong("resetTimeout", 30000L))
    );
  }

  protected Single<Integer> publishHttpEndpoint(String name, String host, int port, String apiName) {
    if (StringUtils.isEmpty(apiName)) {
      apiName = "api.name";
    }
    Record record = HttpEndpoint.createRecord(name, host, port, "/",
      new JsonObject().put("api.name", config().getString(apiName, ""))
    );
    return publish(record);
  }

  protected Single<Integer> publishApiGateway(String host, int port) {
    Record record = HttpEndpoint.createRecord("api-gateway", true, host, port, "/", null)
            .setType("api-gateway");
    return publish(record);
  }

  protected Single<Integer> publishMessageSource(String name, String address) {
    Record record = MessageSource.createRecord(name, address);
    return publish(record);
  }

  protected Single<Integer> publishJDBCDataSource(String name, JsonObject location) {
    Record record = JDBCDataSource.createRecord(name, location, new JsonObject());
    return publish(record);
  }

  protected Single<Integer> publishEventBusService(String name, String address, Class serviceClass) {
    Record record = EventBusService.createRecord(name, address, serviceClass);
    return publish(record);
  }

  private Single<Integer> publish(Record record) {
    return discovery.rxPublish(record).map(r -> {
      registeredRecords.add(r);
      LOGGER.info("Service <" + r.getName() + "> published");
      return 1;
    });
  }

  @Override
  public void stop(Future<Void> future) throws Exception {
    // TODO: to optimize.
    Observable.fromIterable(registeredRecords)
            .flatMap(record -> discovery.rxUnpublish(record.getRegistration()).toObservable())
            .reduce((Void) null, (a, b) -> null)
            .subscribe(future::complete, future::fail);
  }
}
