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

import de.ks.strudel.route.HttpMethod;
import de.ks.strudel.route.Route;
import io.undertow.server.HttpServerExchange;
import org.junit.jupiter.api.Assertions;

import javax.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class TestMetricsCallback implements MetricsCallback {
  final List<Exception> exceptions = new CopyOnWriteArrayList<>();
  final List<Route> routeExecutions = new CopyOnWriteArrayList<>();
  final Map<String, Long> routeTime = new ConcurrentHashMap<>();
  final AtomicLong syncCalls = new AtomicLong();
  final AtomicLong asyncCalls = new AtomicLong();
  final AtomicLong exchanges = new AtomicLong();
  final List<String> unknownRoutes = new CopyOnWriteArrayList<>();

  @Override
  public void trackException(HttpServerExchange exchange, Exception e) {
    Assertions.assertNotNull(exchange);
    Assertions.assertNotNull(e);
    exceptions.add(e);
  }

  @Override
  public void trackRouteExecuted(HttpServerExchange exchange, Route route) {
    Assertions.assertNotNull(exchange);
    routeExecutions.add(route);
  }

  @Override
  public void trackRouteExecutionTime(HttpServerExchange exchange, Route route, long timeInNs) {
    Assertions.assertNotNull(exchange);
    routeTime.put(route.getPath(), timeInNs);
  }

  @Override
  public void trackSyncRouteCall() {
    syncCalls.incrementAndGet();
  }

  @Override
  public void trackAsyncRouteCall() {
    asyncCalls.incrementAndGet();
  }

  @Override
  public void trackExchange(HttpServerExchange exchange) {
    Assertions.assertNotNull(exchange);
    exchanges.incrementAndGet();
  }

  @Override
  public void trackUnknownRoute(HttpServerExchange exchange, HttpMethod method, String url) {
    Assertions.assertNotNull(exchange);
    Assertions.assertNotNull(method);
    Assertions.assertNotNull(url);
    unknownRoutes.add(url);
  }
}
