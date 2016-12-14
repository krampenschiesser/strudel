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
import de.ks.strudel.route.Router;
import de.ks.strudel.template.ModelAndView;
import de.ks.strudel.template.TemplateEngine;
import de.ks.strudel.template.TemplateEngineResolver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class RenderingAndExecutionHandler implements HttpHandler {
  private final Request request;
  private final Response response;
  private final Route route;
  private final TemplateEngineResolver templateEngineResolver;

  public RenderingAndExecutionHandler(Request request, Response response, Route route, TemplateEngineResolver templateEngineResolver) {
    this.request = request;
    this.response = response;
    this.route = route;
    this.templateEngineResolver = templateEngineResolver;
  }

  @Override
  public void handleRequest(HttpServerExchange ex) throws Exception {
    Object retval = route.getHandler().handle(request, response);
    if (route.getTemplateEngine() != null) {
      retval = renderTemplate(retval);
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
  }

  private Object renderTemplate(Object retval) throws Exception {
    if (!(retval instanceof ModelAndView)) {
      throw new IllegalStateException("in template route " + route.getPath() + " a " + ModelAndView.class.getSimpleName() + " needs to be returned");
    }
    @SuppressWarnings("unchecked")
    ModelAndView mav = (ModelAndView) retval;
    TemplateEngine templateEngine = templateEngineResolver.getTemplateEngine(route.getTemplateEngine());
    retval = templateEngine.render(mav.getModel(), mav.getTemplateName());
    return retval;
  }
}
