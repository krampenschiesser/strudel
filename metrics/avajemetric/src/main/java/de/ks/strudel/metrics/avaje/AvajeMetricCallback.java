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

import de.ks.strudel.metrics.ExceptionHistory;
import de.ks.strudel.metrics.MetricsCallback;
import de.ks.strudel.metrics.StoredException;
import de.ks.strudel.route.HttpMethod;
import de.ks.strudel.route.Route;
import io.undertow.server.HttpServerExchange;
import org.avaje.metric.MetricName;
import org.avaje.metric.TimedEvent;
import org.avaje.metric.core.DefaultMetricName;
import org.avaje.metric.spi.PluginMetricManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
public class AvajeMetricCallback implements MetricsCallback<TimedEvent> {
  public static final MetricName asyncCount = new DefaultMetricName("de.ks", "async", "count");
  public static final MetricName syncCount = new DefaultMetricName("de.ks", "sync", "count");
  public static final MetricName exchangeCount = new DefaultMetricName("de.ks", "exchange", "count");
  public static final MetricName exceptionCount = new DefaultMetricName("de.ks", "exception", "count");

  public static MetricName routeExecution(Route route) {
    return routeExecution(route.getMethod(), route.getPath());
  }

  public static MetricName routeExecution(HttpMethod method, String path) {
    return new DefaultMetricName("route", method.name(), path);
  }

  public static MetricName routeExecutionTime(Route route) {
    return routeExecutionTime(route.getMethod(), route.getPath());
  }

  public static MetricName routeExecutionTime(HttpMethod method, String path) {
    return new DefaultMetricName("routeexecutiontime", method.getMethod().toString(), path);
  }

  public static MetricName unknownRoute(HttpMethod method, String url) {
    return new DefaultMetricName("unknownroute", method.name(), url);
  }

  private final PluginMetricManager metricManager;
  private final int[] buckets;
  private final ExceptionHistory exceptionHistory;

  @Inject
  public AvajeMetricCallback(PluginMetricManager metricManager, @Named(AvajeMetricModule.METRIC_BUCKETS) int[] buckets, ExceptionHistory exceptionHistory) {
    this.metricManager = metricManager;
    this.buckets = buckets;
    this.exceptionHistory = exceptionHistory;
  }

  @Override
  public void trackException(HttpServerExchange exchange, Exception e) {
    metricManager.getCounterMetric(exceptionCount).markEvent();
    exceptionHistory.trackException(e);
  }

  @Override
  public void trackRouteExecuted(HttpServerExchange exchange, Route route) {
    metricManager.getCounterMetric(routeExecution(route)).markEvent();
  }

  @Override
  public void trackRouteExecutionTime(HttpServerExchange exchange, Route route, long timeInNs) {
//    metricManager.getBucketTimedMetric(routeExecutionTime(route), buckets).addEventDuration(true, timeInNs);
  }

  @Override
  public TimedEvent beforeRouteExecution(HttpServerExchange exchange, Route route) {
    return metricManager.getBucketTimedMetric(routeExecutionTime(route), buckets).startEvent();
  }

  @Override
  public void afterRouteExecution(HttpServerExchange exchange, Route route, @Nullable Exception e, TimedEvent stopWatch) {
    stopWatch.end(e == null);
  }

  @Override
  public void trackSyncRouteCall() {
    metricManager.getCounterMetric(syncCount).markEvent();
  }

  @Override
  public void trackAsyncRouteCall() {
    metricManager.getCounterMetric(asyncCount).markEvent();
  }

  @Override
  public void trackExchange(HttpServerExchange exchange) {
    metricManager.getCounterMetric(exchangeCount).markEvent();
  }

  @Override
  public void trackUnknownRoute(HttpServerExchange exchange, HttpMethod method, String url) {
    metricManager.getCounterMetric(unknownRoute(method, url)).markEvent();
  }

  public Collection<StoredException> getStoredExceptions() {
    return exceptionHistory.getExceptions();
  }
}
