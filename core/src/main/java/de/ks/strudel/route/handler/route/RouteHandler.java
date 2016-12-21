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

import de.ks.strudel.metrics.MetricsCallback;
import de.ks.strudel.request.RequestBodyParser;
import de.ks.strudel.request.RequestFormParser;
import de.ks.strudel.route.Route;
import de.ks.strudel.route.handler.ExceptionHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A handler for each registered route.
 * Switching the thread is not allowed anymore at this point.
 * It starts the request scope, the async before/after callbacks  and executes the final rendering handler.
 *
 * One instance per route
 */
public class RouteHandler implements HttpHandler {
  protected final AtomicReference<Route> route = new AtomicReference<>();
  protected final AtomicReference<MetricsCallback> metricsReference = new AtomicReference<>();

  protected final Provider<ExecuteAsAsyncHandler> executeAsAsyncProvider;
  protected final Provider<RequestScopeHandler> requestScopeHandlerProvider;
  protected final Provider<ExceptionHandler> exceptionHandlerProvider;
  protected final Provider<AsyncCallbackHandler> asyncCallbackHandlerProvider;
  protected final Provider<RenderingAndExecutionHandler> finalRouteHandlerProvider;
  protected final Provider<RequestBodyParser> bodyParserProvider;
  protected final Provider<RequestFormParser> formParserProvider;

  @Inject
  public RouteHandler(Provider<ExecuteAsAsyncHandler> executeAsAsyncProvider, Provider<RequestScopeHandler> requestScopeHandlerProvider, Provider<ExceptionHandler> exceptionHandlerProvider, Provider<AsyncCallbackHandler> asyncCallbackHandlerProvider, Provider<RenderingAndExecutionHandler> finalRouteHandlerProvider, Provider<RequestBodyParser> bodyParserProvider, Provider<RequestFormParser> formParserProvider) {
    this.executeAsAsyncProvider = executeAsAsyncProvider;
    this.requestScopeHandlerProvider = requestScopeHandlerProvider;
    this.exceptionHandlerProvider = exceptionHandlerProvider;
    this.asyncCallbackHandlerProvider = asyncCallbackHandlerProvider;
    this.finalRouteHandlerProvider = finalRouteHandlerProvider;
    this.bodyParserProvider = bodyParserProvider;
    this.formParserProvider = formParserProvider;
  }

  @com.google.inject.Inject(optional = true)
  public RouteHandler setMetricsReference(MetricsCallback metrics) {
    this.metricsReference.set(metrics);
    return this;
  }

  public RouteHandler setRoute(Route route) {
    this.route.set(route);
    return this;
  }

  @Override
  public void handleRequest(HttpServerExchange ex) throws Exception {
    RequestBodyParser bodyParser = bodyParserProvider.get();
    RequestFormParser formParser = formParserProvider.get();
    bodyParser.setExchange(ex);
    formParser.setExchange(ex);

    if (metricsReference.get() != null) {
      metricsReference.get().trackRouteExecuted(ex, route.get());
    }

    ExecuteAsAsyncHandler executeAsAsync = executeAsAsyncProvider.get();
    AsyncCallbackHandler asyncCallbackHandler = asyncCallbackHandlerProvider.get();
    RenderingAndExecutionHandler finalRouteHandler = finalRouteHandlerProvider.get();
    RequestScopeHandler requestScopeHandler = requestScopeHandlerProvider.get();
    ExceptionHandler exceptionHandler = exceptionHandlerProvider.get();

    executeAsAsync.setRoute(route.get());
    finalRouteHandler.setRoute(route.get());
    asyncCallbackHandler.setAsyncAfter(route.get().getAsyncAfter()).setAsyncBefore(route.get().getAsyncBefore());
    requestScopeHandler.setFormParser(formParser).setBodyParser(bodyParser);
    requestScopeHandler.setRoute(route.get());


    if (route.get().isAsync()) {
      executeAsAsync.setNext(requestScopeHandler);
      requestScopeHandler.setNext(exceptionHandler);
      exceptionHandler.setNext(asyncCallbackHandler);
      asyncCallbackHandler.setNext(finalRouteHandler);

      executeAsAsync.handleRequest(ex);
    } else {
      requestScopeHandler.setNext(asyncCallbackHandler);
      asyncCallbackHandler.setNext(finalRouteHandler);

      requestScopeHandler.handleRequest(ex);
    }
  }
}
