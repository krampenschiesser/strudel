/**
 * Copyright [2016] [Christian Loehnert]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.strudel.metrics;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.strudel.Strudel;
import de.ks.strudel.StrudelModule;
import de.ks.strudel.route.HttpMethod;
import de.ks.strudel.util.StrudelTestExtension;
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

public class MetricsTest {
  private static final Logger log = LoggerFactory.getLogger(MetricsTest.class);

  private Strudel strudel;

  @AfterEach
  public void afterEach() throws Exception {
    strudel.stop();
  }

  @BeforeEach
  public void beforeEach() throws Exception {
    Injector injector = Guice.createInjector(new StrudelModule(), new MetricsTestModule());

    strudel = injector.getInstance(Strudel.class);
    strudel.options().port(StrudelTestExtension.port);
    RestAssured.port = StrudelTestExtension.port;
    RestAssured.defaultParser = Parser.HTML;

    injector.injectMembers(this);
  }

  @Inject
  TestMetricsCallback metricsCallback;

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

    assertEquals(2, metricsCallback.exceptions.size());
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

    assertEquals(5, metricsCallback.routeExecutions.size());
    long getCalls = metricsCallback.routeExecutions.stream().filter(r -> r.getMethod() == HttpMethod.GET).count();
    assertEquals(2, getCalls);

    assertEquals(3, metricsCallback.syncCalls.get());
    assertEquals(2, metricsCallback.asyncCalls.get());

    assertEquals(5, metricsCallback.exchanges.get());
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

    RestAssured.get("/");
    RestAssured.get("/get");

    Long timeNs = metricsCallback.routeTime.get("/get");
    assertNotNull(timeNs);
    assertThat(timeNs).isGreaterThan(sleeptime);
    log.info("/get took {}µs", TimeUnit.NANOSECONDS.toMicros(timeNs));

    timeNs = metricsCallback.routeTime.get("/");
    assertNotNull(timeNs);
    log.info("/ took {}µs", TimeUnit.NANOSECONDS.toMicros(timeNs));
  }

  @Test
  void unknownRoutes() {
    strudel.get("/", (request, response) -> "");
    strudel.start();

    RestAssured.get("/");
    RestAssured.put("/");
    RestAssured.get("/other");
    RestAssured.put("/unknown");

    assertThat(metricsCallback.unknownRoutes).contains("/unknown", "/", "/other");
  }
}
