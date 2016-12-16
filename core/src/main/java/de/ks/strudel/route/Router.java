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
import de.ks.strudel.json.JsonResolver;
import de.ks.strudel.localization.LocaleResolver;
import de.ks.strudel.route.handler.MainHandler;
import de.ks.strudel.route.handler.RouteHandler;
import de.ks.strudel.scope.RequestScope;
import de.ks.strudel.template.TemplateEngineResolver;
import io.undertow.Handlers;
import io.undertow.attribute.ResponseHeaderAttribute;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.util.CopyOnWriteMap;
import io.undertow.util.Headers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Big behemoth handling registering the routes after {@link Strudel#start()}
 * Need to simplify this
 */
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
  final Map<Class<? extends Exception>, Supplier<HandlerNoReturn>> exceptionMappings = new CopyOnWriteMap<>();
  final ThreadLocal<Boolean> asyncRoute = new ThreadLocal<>();
  final Injector injector;
  final RequestScope requestScope;
  final TemplateEngineResolver templateEngineResolver;
  final LocaleResolver localeResolver;
  private final JsonResolver jsonResolver;
  private final Provider<Locale> localeProvider;

  @Inject public Router(Injector injector, RequestScope requestScope, TemplateEngineResolver templateEngineResolver, LocaleResolver localeResolver, JsonResolver jsonResolver, Provider<Locale> localeProvider) {
    this.injector = injector;
    this.requestScope = requestScope;
    this.templateEngineResolver = templateEngineResolver;
    this.localeResolver = localeResolver;
    this.jsonResolver = jsonResolver;
    this.localeProvider = localeProvider;

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

    mainHandler = new MainHandler(asyncRoute, exceptionMappings, before, routing, after, localeProvider);
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

    RoutingHandler currentRoutingHandler;
    if (route.isFilter()) {
      currentRoutingHandler = route.getFilterType() == FilterType.BEFORE ? before : after;
    } else {
      currentRoutingHandler = routing;
    }
    route.getMethods().forEach(m -> currentRoutingHandler.add(m, route.getPath(), httpHandler));
  }

  private HttpHandler createRouteHandler(Route route) {
    return new RouteHandler(route, requestScope, asyncRoute, templateEngineResolver, jsonResolver, localeResolver, exceptionMappings, localeProvider);
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
