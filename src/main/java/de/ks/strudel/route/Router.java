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

import de.ks.strudel.Request;
import de.ks.strudel.Response;
import io.undertow.Handlers;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.util.Headers;

import javax.inject.Singleton;

@Singleton
public class Router {
  public static final int MTU = 1480;
  private final RoutingHandler routing;
  private Predicate contentSizeAbove100 = Predicates.maxContentSize(MTU);

  public Router() {
    routing = Handlers.routing();
  }

  private HttpHandler wrapInGzipHandler(HttpHandler handler) {
    ContentEncodingRepository repo = new ContentEncodingRepository();
    repo.addEncodingHandler("gzip", new GzipEncodingProvider(), 50, contentSizeAbove100);//priority unknown
    EncodingHandler encodingHandler = new EncodingHandler(repo);
    encodingHandler.setNext(handler);
    return encodingHandler;
  }

  public void addRoute(Route route) {
    HttpHandler httpHandler = ex -> {
      Object retval = route.getHandler().handle(new Request(ex), new Response(ex));
      if (retval != null) {
        ex.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html;charset=utf-8");
        ex.getResponseSender().send(String.valueOf(retval));
      } else {
        ex.setStatusCode(HttpStatus.NOT_FOUND.getValue()).getResponseSender().close();
      }
    };
    if (route.isGzip()) {
      httpHandler = wrapInGzipHandler(httpHandler);
    }
    routing.add(route.getMethod().getMethod(), route.getPath(), httpHandler);
  }

  public HttpHandler getHandler() {
    return routing;
  }
}
