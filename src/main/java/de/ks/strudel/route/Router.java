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
import de.ks.strudel.HaltException;
import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Request;
import de.ks.strudel.Response;
import de.ks.strudel.template.ModelAndView;
import de.ks.strudel.template.TemplateEngine;
import io.undertow.Handlers;
import io.undertow.attribute.ResponseHeaderAttribute;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.util.Headers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Router {
  public static final int MTU = 1480;
  private final RoutingHandler routing;
  private final RoutingHandler before;
  private final Predicate contentSizeAbove100 = Predicates.or(//
    Predicates.not(Predicates.exists(new ResponseHeaderAttribute(Headers.CONTENT_LENGTH))),//Currently undertow does not always set the content length
    Predicates.maxContentSize(MTU));
  private final RoutingHandler after;
  private final HttpHandler mainHandler;
  private final ConcurrentHashMap<Class<? extends Exception>, HandlerNoReturn> exceptionMappings = new ConcurrentHashMap<>();
  private final ThreadLocal<Boolean> asyncRoute = new ThreadLocal<>();
  private final Injector injector;

  @Inject
  public Router(Injector injector) {
    this.injector = injector;

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

    mainHandler = exchange -> {
      try {
        asyncRoute.set(false);
        try {
          before.handleRequest(exchange);
          if (!exchange.isComplete()) {
            routing.handleRequest(exchange);
          }
        } catch (HaltException e) {
          throw e;
        } catch (Exception e) {
          HandlerNoReturn handler = exceptionMappings.get(e.getClass());
          if (handler == null) {
            handler = exceptionMappings.entrySet().stream().filter(entry -> entry.getKey().isAssignableFrom(e.getClass())).map(Map.Entry::getValue).findFirst().orElse(null);
          }
          if (handler != null) {
            handler.handle(new Request(exchange), new Response(exchange));
            if (!exchange.isResponseStarted() && exchange.getStatusCode() == 200) {
              exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.getValue());
            }
          } else {
            throw e;
          }
        }
        if (asyncRoute.get()) {
          asyncRoute.set(false);
          return;
        } else {
          after.handleRequest(exchange);

          boolean inProgress = (exchange.isResponseStarted() || exchange.isDispatched());
          if (!inProgress && exchange.getStatusCode() == 200) {
            exchange.setStatusCode(HttpStatus.NOT_FOUND.getValue());
          }
          if (!exchange.isComplete() && !inProgress) {
            exchange.endExchange();
          }
        }
      } catch (HaltException e) {
        exchange.setStatusCode(e.getStatus());
        exchange.endExchange();
      }
    };
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

    RoutingHandler rh;
    if (route.isFilter()) {
      rh = route.getFilterType() == FilterType.BEFORE ? before : after;
    } else {
      rh = routing;
    }
    route.getMethods().forEach(m -> rh.add(m, route.getPath(), httpHandler));
  }

  private HttpHandler createRouteHandler(Route route) {
    return new HttpHandler() {
      @Override
      public void handleRequest(HttpServerExchange ex) throws Exception {
        if (route.isAsync() && ex.isInIoThread()) {
          asyncRoute.set(true);
          ex.dispatch(this);
          return;
        }
        Request request = new Request(ex);
        Response response = new Response(ex);
        HandlerNoReturn asyncBefore = route.getAsyncBefore();
        HandlerNoReturn asyncAfter = route.getAsyncAfter();
        if (asyncBefore != null) {
          asyncBefore.handle(request, response);
        }
        try {
          Object retval = route.getHandler().handle(request, response);
          if (route.getTemplateEngine() != null) {
            if (!(retval instanceof ModelAndView)) {
              throw new IllegalStateException("in template route " + route.getPath() + " a " + ModelAndView.class.getSimpleName() + " needs to be returned");
            }
            @SuppressWarnings("unchecked")
            ModelAndView mav = (ModelAndView) retval;
            TemplateEngine templateEngine = injector.getInstance(route.getTemplateEngine());
            retval = templateEngine.render(mav.getModel(), mav.getTemplateName());
          }
          if (retval != null) {
            String data = String.valueOf(retval);
            int length = data.getBytes().length;
            if (length < MTU) {
              ex.getResponseHeaders().add(Headers.CONTENT_LENGTH, length);//workaround, for some reason undertow doesn't always set this
            }
            ex.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html;charset=utf-8");
            ex.getResponseSender().send(data, new NoCompletionCallback());
          }
        } finally {
          if (asyncAfter != null) {
            asyncAfter.handle(request, response);
          }
        }
      }
    };
  }

  public HttpHandler getHandler() {
    return mainHandler;
  }

  public void addExceptionHandler(Class<? extends Exception> clazz, HandlerNoReturn handler) {
    exceptionMappings.put(clazz, handler);
  }
}
