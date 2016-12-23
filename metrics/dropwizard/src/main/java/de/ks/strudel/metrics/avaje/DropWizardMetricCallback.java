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
package de.ks.strudel.metrics.avaje;

import com.codahale.metrics.MetricRegistry;
import de.ks.strudel.metrics.ExceptionHistory;
import de.ks.strudel.metrics.MetricsCallback;
import de.ks.strudel.metrics.StoredException;
import de.ks.strudel.route.HttpMethod;
import de.ks.strudel.route.Route;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Singleton
public class DropWizardMetricCallback implements MetricsCallback {
  public static final String asyncCount = "syncRouteCallCount";
  public static final String syncCount = "asyncRouteCallCount";
  public static final String exchangeCount = "exchangeCount";
  public static final String exceptionCount = "exceptionCount";
  public static final int MAX_EXCEPTIONS = 100;

  public static String routeExecution(Route route) {
    return routeExecution(route.getMethod(), route.getPath());
  }

  public static String routeExecution(HttpMethod method, String path) {
    return "call:" + method + ":" + path;
  }

  public static String routeExecutionTime(Route route) {
    return routeExecutionTime(route.getMethod(), route.getPath());
  }

  public static String routeExecutionTime(HttpMethod method, String path) {
    return "time:" + method.getMethod() + ":" + path;
  }

  public static String unknownRoute(HttpMethod method, String url) {
    return "unknownroute:" + method.name() + ":" + url;
  }

  private final MetricRegistry metrics;
  private final ExceptionHistory exceptionHistory;

  @Inject
  public DropWizardMetricCallback(MetricRegistry metrics, ExceptionHistory exceptionHistory) {
    this.metrics = metrics;
    this.exceptionHistory = exceptionHistory;
  }

  @Override
  public void trackException(HttpServerExchange exchange, Exception e) {
    metrics.counter(exceptionCount).inc();
    exceptionHistory.trackException(e);
  }

  @Override
  public void trackRouteExecuted(HttpServerExchange exchange, Route route) {
    metrics.counter(routeExecution(route)).inc();
  }

  @Override
  public void trackRouteExecutionTime(HttpServerExchange exchange, Route route, long timeInNs) {
    metrics.timer(routeExecutionTime(route)).update(timeInNs, TimeUnit.NANOSECONDS);
  }

  @Override
  public void trackSyncRouteCall() {
    metrics.counter(syncCount).inc();
  }

  @Override
  public void trackAsyncRouteCall() {
    metrics.counter(asyncCount).inc();
  }

  @Override
  public void trackExchange(HttpServerExchange exchange) {
    metrics.counter(exchangeCount).inc();
  }

  @Override
  public void trackUnknownRoute(HttpServerExchange exchange, HttpMethod method, String url) {
    metrics.counter(unknownRoute(method, url)).inc();
  }

  public Collection<StoredException> getStoredExceptions() {
    return exceptionHistory.getExceptions();
  }
}
