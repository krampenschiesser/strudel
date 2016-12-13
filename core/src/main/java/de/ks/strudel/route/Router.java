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
import de.ks.strudel.scope.RequestScope;
import io.undertow.Handlers;
import io.undertow.attribute.ResponseHeaderAttribute;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.util.Headers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Singleton
public class Router {
  public static final int MTU = 1480;
  final RoutingHandler routing;
  final RoutingHandler before;
  final Predicate contentSizeAbove100 = Predicates.or(//
    Predicates.not(Predicates.exists(new ResponseHeaderAttribute(Headers.CONTENT_LENGTH))),//Currently undertow does not always set the content length
    Predicates.maxContentSize(MTU));
  final RoutingHandler after;
  final HttpHandler mainHandler;
  final ConcurrentHashMap<Class<? extends Exception>, Supplier<HandlerNoReturn>> exceptionMappings = new ConcurrentHashMap<>();
  final ThreadLocal<Boolean> asyncRoute = new ThreadLocal<>();
  final Injector injector;
  final RequestScope requestScope;

  @Inject
  public Router(Injector injector, RequestScope requestScope) {
    this.injector = injector;
    this.requestScope = requestScope;

    routing = Handlers.routing();
    before = Handlers.routing();
    after = Handlers.routing();
    HttpHandler noop = ex -> "".toCharArray();
    before.setFallbackHandler(noop);
    before.setInvalidMethodHandler(null);
    after.setFallbackHandler(noop);
    after.setInvalidMethodHandler(null);
    routing.setFallbackHandler(after);
    routing.setInvalidMethodHandler(after);

    mainHandler = new MainHandler(this);
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

    if (route.isGzip()) {
      httpHandler = wrapInGzipHandler(createRouteHandler(route));
    } else {
      httpHandler = createRouteHandler(route);
    }

    RoutingHandler routingHandler;
    if (route.isFilter()) {
      routingHandler = route.getFilterType() == FilterType.BEFORE ? before : after;
    } else {
      routingHandler = routing;
    }
    route.getMethods().forEach(m -> routingHandler.add(m, route.getPath(), httpHandler));
  }

  private HttpHandler createRouteHandler(Route route) {
    return new RouteHandler(route, requestScope, asyncRoute, injector);
  }

  public void addExceptionHandler(Class<? extends Exception> clazz, Class<? extends HandlerNoReturn> handler) {
    exceptionMappings.put(clazz, () -> injector.getInstance(handler));
  }

  public void addExceptionHandler(Class<? extends Exception> clazz, HandlerNoReturn handler) {
    exceptionMappings.put(clazz, () -> handler);
  }

  public HttpHandler getHandler() {
    return mainHandler;
  }

}
