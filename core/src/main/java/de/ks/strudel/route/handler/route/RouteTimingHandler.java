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
package de.ks.strudel.route.handler.route;

import com.google.inject.Inject;
import de.ks.strudel.metrics.MetricsCallback;
import de.ks.strudel.route.Route;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class RouteTimingHandler implements HttpHandler {
  private RequestScopeHandler next;
  private MetricsCallback metricsCallback;
  private Route route;

  @Inject(optional = true)
  public RouteTimingHandler setMetricsCallback(MetricsCallback metricsCallback) {
    this.metricsCallback = metricsCallback;
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    if (metricsCallback != null) {
      Object o = metricsCallback.beforeRouteExecution(exchange, route);
      long startTime = System.nanoTime();
      try {
        next.handleRequest(exchange);
        metricsCallback.afterRouteExecution(exchange, route, null, o);
      } catch (Exception e) {
        metricsCallback.afterRouteExecution(exchange, route, e, o);
        throw e;
      } finally {
        after(exchange, System.nanoTime() - startTime);
      }
    } else {
      next.handleRequest(exchange);
    }
  }

  private void after(HttpServerExchange exchange, long took) {
    if (route.isAsync()) {
      metricsCallback.trackAsyncRouteCall();
    } else {
      metricsCallback.trackSyncRouteCall();
    }
    metricsCallback.trackRouteExecutionTime(exchange, route, took);
  }

  public void setNext(RequestScopeHandler next) {
    this.next = next;
  }

  public RouteTimingHandler setRoute(Route route) {
    this.route = route;
    return this;
  }
}
