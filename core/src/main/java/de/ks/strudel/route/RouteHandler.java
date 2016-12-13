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
import de.ks.strudel.Request;
import de.ks.strudel.Response;
import de.ks.strudel.scope.RequestScope;
import de.ks.strudel.template.ModelAndView;
import de.ks.strudel.template.TemplateEngine;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

class RouteHandler implements HttpHandler {
  private final Route route;
  private final RequestScope requestScope;
  private final ThreadLocal<Boolean> asyncRoute;
  private final Injector injector;

  public RouteHandler(Route route, RequestScope requestScope, ThreadLocal<Boolean> asyncRoute, Injector injector) {
    this.route = route;
    this.requestScope = requestScope;
    this.asyncRoute = asyncRoute;
    this.injector = injector;
  }

  @Override
  public void handleRequest(HttpServerExchange ex) throws Exception {
    if (route.isAsync() && ex.isInIoThread()) {
      asyncRoute.set(true);
      ex.dispatch(this);
      return;
    }
    Request request = new Request(ex);
    Response response = new Response(ex);
    requestScope.enter(request, response);
    try {
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
          if (length < Router.MTU) {
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
    } finally {
      requestScope.exit();
    }
  }
}
