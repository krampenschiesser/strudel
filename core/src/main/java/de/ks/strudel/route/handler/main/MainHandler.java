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
package de.ks.strudel.route.handler.main;

import de.ks.strudel.metrics.MetricsCallback;
import de.ks.strudel.route.handler.ExceptionHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wrapper that is active over the lifecycle of the application
 * Provides a basic facade around the 3 routing handlers: before, main after.
 */
@Singleton
public class MainHandler implements HttpHandler {
  protected final AsyncRouteHandler asyncRouteHandler;
  protected final EndExchangeHandler endExchangeHandler;
  protected final ExceptionHandler exceptionHandler;
  protected final BeforeAfterMainHandler beforeAfterMainHandler;

  protected final AtomicReference<MetricsCallback> metricsReference = new AtomicReference<>();

  @Inject
  public MainHandler(AsyncRouteHandler asyncRouteHandler, EndExchangeHandler endExchangeHandler, ExceptionHandler exceptionHandler, BeforeAfterMainHandler beforeAfterMainHandler) {
    this.asyncRouteHandler = asyncRouteHandler;
    this.endExchangeHandler = endExchangeHandler;
    this.exceptionHandler = exceptionHandler;
    this.beforeAfterMainHandler = beforeAfterMainHandler;
  }

  @com.google.inject.Inject(optional = true)
  public MainHandler setMetricsReference(MetricsCallback metrics) {
    this.metricsReference.set(metrics);
    return this;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    if (metricsReference.get() != null) {
      metricsReference.get().trackExchange(exchange);
    }
    asyncRouteHandler.setNext(endExchangeHandler);
    endExchangeHandler.setNext(exceptionHandler);
    exceptionHandler.setNext(beforeAfterMainHandler);

    asyncRouteHandler.handleRequest(exchange);
  }
}
