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

/**
 * Interface used for metrics.
 * Once an instance is bound to this interface metrics will be automatically collected.
 */
public interface MetricsCallback {
  /**
   * Tracks exceptions beeing thrown by handlers
   *
   * @param exchange e
   * @param e        e
   */
  void trackException(HttpServerExchange exchange, Exception e);

  /**
   * Tracks a call of a route(before executing the handler)
   * @param exchange e
   * @param route r
   */
  void trackRouteExecuted(HttpServerExchange exchange, Route route);

  /**
   * Tracks execution time of a route. Use this to detect slow handlers
   * @param exchange e
   * @param route r
   * @param timeInNs nanosecond
   */
  void trackRouteExecutionTime(HttpServerExchange exchange, Route route, long timeInNs);

  /**
   * For counting how many synchronous routes were called
   */
  void trackSyncRouteCall();

  /**
   * For counting how many asynchronous routes were called
   */
  void trackAsyncRouteCall();

  /**
   * Tracks every exchange that is passed into the root handler
   * @param exchange e
   */
  void trackExchange(HttpServerExchange exchange);

  /**
   * Tracks calls to routes that are not mapped.
   *
   * @param exchange e
   * @param method http method of the call
   * @param url of the call
   */
  void trackUnknownRoute(HttpServerExchange exchange, HttpMethod method, String url);
}
