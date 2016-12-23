package de.ks.strudel.metrics.avaje;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.strudel.Strudel;
import de.ks.strudel.StrudelModule;
import de.ks.strudel.route.HttpMethod;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DropWizardMetricCallbackTest {
  private static final Logger log = LoggerFactory.getLogger(DropWizardMetricCallbackTest.class);
  public static final int PORT = 7777;

  private Strudel strudel;

  @AfterEach
  public void afterEach() throws Exception {
    strudel.stop();
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    Injector injector = Guice.createInjector(new StrudelModule(), new DropwizardMetricModule());

    strudel = injector.getInstance(Strudel.class);
    strudel.options().port(PORT);
    RestAssured.port = PORT;
    RestAssured.defaultParser = Parser.HTML;

    injector.injectMembers(this);
  }

  @Inject
  MetricRegistry metrics;

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


    assertEquals(2, metrics.counter(DropWizardMetricCallback.exceptionCount).getCount());
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


    long count = metrics.counter(DropWizardMetricCallback.routeExecution(HttpMethod.GET, "/get")).getCount();
    assertEquals(2, count);

    count = metrics.counter(DropWizardMetricCallback.routeExecution(HttpMethod.PUT, "/put")).getCount();
    assertEquals(1, count);
    count = metrics.counter(DropWizardMetricCallback.routeExecution(HttpMethod.POST, "/post")).getCount();
    assertEquals(1, count);
    count = metrics.counter(DropWizardMetricCallback.routeExecution(HttpMethod.DELETE, "/delete")).getCount();
    assertEquals(1, count);

    count = metrics.counter(DropWizardMetricCallback.syncCount).getCount();
    assertEquals(3, count);
    count = metrics.counter(DropWizardMetricCallback.asyncCount).getCount();
    assertEquals(2, count);

    assertEquals(5, metrics.counter(DropWizardMetricCallback.exchangeCount).getCount());
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

    long timeNs = metrics.timer(DropWizardMetricCallback.routeExecutionTime(HttpMethod.GET, "/get")).getSnapshot().getMax();
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

    long count = metrics.counter(DropWizardMetricCallback.unknownRoute(HttpMethod.GET, "/other")).getCount();
    assertEquals(1, count);
    count = metrics.counter(DropWizardMetricCallback.unknownRoute(HttpMethod.PUT, "/")).getCount();
    assertEquals(1, count);
    count = metrics.counter(DropWizardMetricCallback.unknownRoute(HttpMethod.PUT, "/unknown")).getCount();
    assertEquals(1, count);
  }
}