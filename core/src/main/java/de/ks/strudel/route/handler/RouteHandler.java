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
package de.ks.strudel.route.handler;

import de.ks.strudel.Request;
import de.ks.strudel.Response;
import de.ks.strudel.route.Route;
import de.ks.strudel.scope.RequestScope;
import de.ks.strudel.template.TemplateEngineResolver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class RouteHandler implements HttpHandler {
  private final Route route;
  private final RequestScope requestScope;
  private final ThreadLocal<Boolean> asyncRoute;
  private final TemplateEngineResolver templateEngineResolver;

  public RouteHandler(Route route, RequestScope requestScope, ThreadLocal<Boolean> asyncRoute, TemplateEngineResolver templateEngineResolver) {
    this.route = route;
    this.requestScope = requestScope;
    this.asyncRoute = asyncRoute;
    this.templateEngineResolver = templateEngineResolver;
  }

  @Override
  public void handleRequest(HttpServerExchange ex) throws Exception {
    Request request = new Request(ex);
    Response response = new Response(ex);

    ExecuteAsAsyncHandler executeAsAsync = new ExecuteAsAsyncHandler(route, asyncRoute);
    RequestScopeHandler requestScopeHandler = new RequestScopeHandler(requestScope);
    AsyncCallbackHandler asyncCallbackHandler = new AsyncCallbackHandler(route.getAsyncBefore(), route.getAsyncAfter(), request, response);
    RenderingAndExecutionHandler finalRouteHandler = new RenderingAndExecutionHandler(request, response, route, templateEngineResolver);
    executeAsAsync.setNext(requestScopeHandler);
    requestScopeHandler.setNext(asyncCallbackHandler);
    asyncCallbackHandler.setNext(finalRouteHandler);

    executeAsAsync.handleRequest(ex);
  }
}
