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

import com.google.inject.Injector;
import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Strudel;
import de.ks.strudel.route.handler.ExceptionMappingRegistry;
import de.ks.strudel.route.handler.main.AfterHandler;
import de.ks.strudel.route.handler.main.BeforeHandler;
import de.ks.strudel.route.handler.main.MainHandler;
import de.ks.strudel.route.handler.main.MainRoutingHandler;
import de.ks.strudel.route.handler.route.RouteHandler;
import io.undertow.Handlers;
import io.undertow.attribute.ResponseHeaderAttribute;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.util.Headers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Big behemoth handling registering the routes after {@link Strudel#start()}
 * Need to simplify this
 */
@Singleton
public class Router {
  public static final int MTU = 1480;
  final MainRoutingHandler routing;
  final BeforeHandler before;
  final AfterHandler after;
  private final Provider<RouteHandler> routeHandlerProvider;

  final Predicate contentSizeAbove100 = Predicates.or(//
    Predicates.not(Predicates.exists(new ResponseHeaderAttribute(Headers.CONTENT_LENGTH))),//Currently undertow does not always set the content length
    Predicates.maxContentSize(MTU));
  private final HttpHandler mainHandler;
  private final ExceptionMappingRegistry exceptionMappingRegistry;
  private final Injector injector;

  @Inject
  public Router(Injector injector, MainHandler mainHandler, BeforeHandler before, MainRoutingHandler routing, AfterHandler after, Provider<RouteHandler> routeHandlerProvider, ExceptionMappingRegistry exceptionMappingRegistry) {
    this.injector = injector;
    this.mainHandler = mainHandler;
    this.before = before;
    this.routing = routing;
    this.after = after;
    this.routeHandlerProvider = routeHandlerProvider;
    this.exceptionMappingRegistry = exceptionMappingRegistry;

    HttpHandler noop = ex -> "".toCharArray();
    this.before.setFallbackHandler(noop);
    this.before.setInvalidMethodHandler(null);
    this.after.setFallbackHandler(noop);
    this.after.setInvalidMethodHandler(null);
    this.routing.setFallbackHandler(after);
    this.routing.setInvalidMethodHandler(after);
  }

  private HttpHandler wrapInGzipHandler(HttpHandler handler) {
    ContentEncodingRepository repo = new ContentEncodingRepository();
    repo.addEncodingHandler("gzip", new GzipEncodingProvider(), 50, contentSizeAbove100);//undertow doesn't always set content length
    EncodingHandler encodingHandler = new EncodingHandler(repo);
    encodingHandler.setNext(handler);
    return encodingHandler;
  }

  public void addRoute(Route route) {
    HttpHandler httpHandler;

//    CacheHandler cacheHandler = new CacheHandler(new DirectBufferCache(100, 100, 10000));
    HttpHandler routeHandler = createRouteHandler(route);
//    if (route.isEtag()) {
//      cacheHandler.setNext(routeHandler);
//      routeHandler = cacheHandler;
//    }
    if (route.isGzip()) {
      httpHandler = wrapInGzipHandler(routeHandler);
    } else if (route.isWebsocket()) {
      httpHandler = Handlers.websocket(route.getWebSocketConnectionCallback());
    } else {
      httpHandler = routeHandler;
    }

    io.undertow.server.RoutingHandler currentRoutingHandler;
    if (route.isFilter()) {
      currentRoutingHandler = route.getFilterType() == FilterType.BEFORE ? before : after;
    } else {
      currentRoutingHandler = routing;
    }
    route.getMethods().forEach(m -> currentRoutingHandler.add(m, route.getPath(), httpHandler));
  }

  private HttpHandler createRouteHandler(Route route) {
    RouteHandler routeHandler = routeHandlerProvider.get();
    routeHandler.setRoute(route);
    return routeHandler;
  }

  public void addExceptionHandler(Class<? extends Exception> clazz, Class<? extends HandlerNoReturn> handler) {
    exceptionMappingRegistry.getExceptionMappings().put(clazz, () -> injector.getInstance(handler));
  }

  public void addExceptionHandler(Class<? extends Exception> clazz, HandlerNoReturn handler) {
    exceptionMappingRegistry.getExceptionMappings().put(clazz, () -> handler);
  }

  public HttpHandler getHandler() {
    return mainHandler;
  }
}
