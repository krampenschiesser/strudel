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

import de.ks.strudel.HandlerNoReturn;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MainHandler implements HttpHandler {

  private final ThreadLocal<Boolean> asyncRoute;
  private final ConcurrentHashMap<Class<? extends Exception>, Supplier<HandlerNoReturn>> exceptionMappings;
  private final RoutingHandler before;
  private final RoutingHandler main;
  private final RoutingHandler after;

  public MainHandler(ThreadLocal<Boolean> asyncRoute, ConcurrentHashMap<Class<? extends Exception>, Supplier<HandlerNoReturn>> exceptionMappings, RoutingHandler before, RoutingHandler main, RoutingHandler after) {
    this.asyncRoute = asyncRoute;
    this.exceptionMappings = exceptionMappings;
    this.before = before;
    this.main = main;
    this.after = after;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    AsyncRouteHandler asyncRouteHandler = new AsyncRouteHandler(asyncRoute);
    EndExchangeHandler endExchangeHandler = new EndExchangeHandler(asyncRoute);
    ExceptionHandler exceptionHandler = new ExceptionHandler(exceptionMappings);
    BeforeAfterMainHandler beforeAfterMainHandler = new BeforeAfterMainHandler(before, main, after, asyncRoute);

    asyncRouteHandler.setNext(endExchangeHandler);
    endExchangeHandler.setNext(exceptionHandler);
    exceptionHandler.setNext(beforeAfterMainHandler);

    asyncRouteHandler.handleRequest(exchange);
  }
}
