package de.ks.strudel.metrics.avaje;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.strudel.Strudel;
import de.ks.strudel.StrudelModule;
import de.ks.strudel.route.HttpMethod;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.avaje.metric.Metric;
import org.avaje.metric.TimedMetric;
import org.avaje.metric.spi.PluginMetricManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AvajeMetricCallbackTest {
  private static final Logger log = LoggerFactory.getLogger(AvajeMetricCallbackTest.class);
  public static final int PORT = 7777;

  private Strudel strudel;

  @AfterEach
  public void afterEach() throws Exception {
    strudel.stop();
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    Injector injector = Guice.createInjector(new StrudelModule(), new AvajeMetricModule());

    strudel = injector.getInstance(Strudel.class);
    strudel.options().port(PORT);
    RestAssured.port = PORT;
    RestAssured.defaultParser = Parser.HTML;

    injector.injectMembers(this);
  }

  @Inject
  PluginMetricManager metricManager;

  @Test
  void exception() {
    strudel.get("/sync", (request, response) -> {
      throw new RuntimeException("test, its ok");
    });
    strudel.get("/async", (request, response) -> {
      throw new RuntimeException("test, its ok");
    }).async();
    strudel.start();

    RestAssured.get("/sync");
    RestAssured.get("/async");

    metricManager.getMetrics().forEach(Metric::collectStatistics);
    assertEquals(2, metricManager.getCounterMetric(AvajeMetricCallback.exceptionCount).getCollectedStatistics().getCount());
  }

  @Test
  void routeCalls() {
    strudel.get("/get", (request, response) -> "");
    strudel.put("/put", (request, response) -> "");
    strudel.post("/post", (request, response) -> "");
    strudel.delete("/delete", (request, response) -> "");
    strudel.start();

    RestAssured.get("/get");
    RestAssured.put("/put");
    RestAssured.post("/post");
    RestAssured.delete("/delete");

    RestAssured.get("/get");

    metricManager.getMetrics().forEach(Metric::collectStatistics);
    long count = metricManager.getCounterMetric(AvajeMetricCallback.routeExecution(HttpMethod.GET, "/get")).getCollectedStatistics().getCount();
    assertEquals(2, count);

    count = metricManager.getCounterMetric(AvajeMetricCallback.routeExecution(HttpMethod.PUT, "/put")).getCollectedStatistics().getCount();
    assertEquals(1, count);
    count = metricManager.getCounterMetric(AvajeMetricCallback.routeExecution(HttpMethod.POST, "/post")).getCollectedStatistics().getCount();
    assertEquals(1, count);
    count = metricManager.getCounterMetric(AvajeMetricCallback.routeExecution(HttpMethod.DELETE, "/delete")).getCollectedStatistics().getCount();
    assertEquals(1, count);

    count = metricManager.getCounterMetric(AvajeMetricCallback.syncCount).getCollectedStatistics().getCount();
    assertEquals(3, count);
    count = metricManager.getCounterMetric(AvajeMetricCallback.asyncCount).getCollectedStatistics().getCount();
    assertEquals(2, count);

    assertEquals(5, metricManager.getCounterMetric(AvajeMetricCallback.exchangeCount).getCollectedStatistics().getCount());
  }

  @Test
  void routeTime() {
    int sleeptime = 50;
    strudel.get("/get", (request, response) -> {
      Thread.sleep(sleeptime);
      return "";
    });
    strudel.get("/", (request, response) -> "");
    strudel.start();

    RestAssured.get("/get");
    metricManager.getMetrics().forEach(Metric::collectStatistics);

    TimedMetric[] buckets = metricManager.getBucketTimedMetric(AvajeMetricCallback.routeExecutionTime(HttpMethod.GET, "/get")).getBuckets();
    long timeNs = Arrays.asList(buckets).stream().mapToLong(m -> m.getCollectedSuccessStatistics().getMax()).filter(l -> l > 0L).findFirst().getAsLong();
//    Long timeNs = metricsCallback.routeTime.get("/get");
    assertNotNull(timeNs);
    assertThat(timeNs).isGreaterThan(sleeptime);
    log.info("/get took {}µs", TimeUnit.NANOSECONDS.toMicros(timeNs));
  }

  @Test
  void unknownRoutes() {
    strudel.get("/", (request, response) -> "");
    strudel.start();

    RestAssured.get("/");
    RestAssured.put("/");
    RestAssured.get("/other");
    RestAssured.put("/unknown");
    metricManager.getMetrics().forEach(Metric::collectStatistics);

    long count = metricManager.getCounterMetric(AvajeMetricCallback.unknownRoute(HttpMethod.GET, "/other")).getCollectedStatistics().getCount();
    assertEquals(1, count);
    count = metricManager.getCounterMetric(AvajeMetricCallback.unknownRoute(HttpMethod.PUT, "/")).getCollectedStatistics().getCount();
    assertEquals(1, count);
    count = metricManager.getCounterMetric(AvajeMetricCallback.unknownRoute(HttpMethod.PUT, "/unknown")).getCollectedStatistics().getCount();
    assertEquals(1, count);
  }
}