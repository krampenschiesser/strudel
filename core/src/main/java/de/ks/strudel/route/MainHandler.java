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
package de.ks.strudel.route;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

class MainHandler implements HttpHandler {
  private final Router router;

  public MainHandler(Router router) {
    this.router = router;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    AsyncRouteHandler asyncRouteHandler = new AsyncRouteHandler(router.asyncRoute);
    EndExchangeHandler endExchangeHandler = new EndExchangeHandler(router.asyncRoute);
    ExceptionHandler exceptionHandler = new ExceptionHandler(router.exceptionMappings);
    BeforeAfterMainHandler beforeAfterMainHandler = new BeforeAfterMainHandler(router.before, router.routing, router.after, router.asyncRoute);

    asyncRouteHandler.setNext(endExchangeHandler);
    endExchangeHandler.setNext(exceptionHandler);
    exceptionHandler.setNext(beforeAfterMainHandler);

    asyncRouteHandler.handleRequest(exchange);
  }
}
