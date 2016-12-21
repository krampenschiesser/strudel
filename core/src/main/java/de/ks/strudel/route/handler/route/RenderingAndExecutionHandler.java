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
package de.ks.strudel.route.handler.route;

import de.ks.strudel.Response;
import de.ks.strudel.json.JsonParser;
import de.ks.strudel.json.JsonResolver;
import de.ks.strudel.request.Request;
import de.ks.strudel.route.HttpStatus;
import de.ks.strudel.route.Route;
import de.ks.strudel.route.Router;
import de.ks.strudel.template.ModelAndView;
import de.ks.strudel.template.TemplateEngine;
import de.ks.strudel.template.TemplateEngineResolver;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import javax.inject.Inject;
import javax.inject.Provider;
import java.nio.ByteBuffer;

/**
 * The handler that finally does some stuff.
 * It creates an instance of the handler of a route and executes it.
 * The retval might be parsed to json if required or used to render a template.
 * Additionally the exchange.send method is invoked
 */
public class RenderingAndExecutionHandler implements HttpHandler {
  protected final Provider<Request> request;
  protected final Provider<Response> response;
  protected final TemplateEngineResolver templateEngineResolver;
  protected final JsonResolver jsonResolver;
  protected Route route;

  @Inject
  public RenderingAndExecutionHandler(Provider<Request> request, Provider<Response> response, TemplateEngineResolver templateEngineResolver, JsonResolver jsonResolver) {
    this.request = request;
    this.response = response;
    this.templateEngineResolver = templateEngineResolver;
    this.jsonResolver = jsonResolver;
  }

  public RenderingAndExecutionHandler setRoute(Route route) {
    this.route = route;
    return this;
  }

  @Override
  public void handleRequest(HttpServerExchange ex) throws Exception {
    Object retval = route.getHandler().handle(request.get(), response.get());
    if (route.isParseAsJson()) {
      if (retval == null) {
        ex.setStatusCode(HttpStatus.NO_CONTENT.getValue());
        retval = "";
      } else {
        JsonParser jsonParser = jsonResolver.getJsonParser(route.getJsonParser());
        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        retval = jsonParser.toString(retval);
      }
    }
    if (route.getTemplateEngine() != null) {
      retval = renderTemplate(retval);
    }
    if (retval != null) {
      if (retval instanceof byte[]) {
        @SuppressWarnings("unchecked")
        byte[] bytes = (byte[]) retval;
        if (ex.getRequestHeaders().get(Headers.CONTENT_LENGTH) == null) {
          ex.getResponseHeaders().add(Headers.CONTENT_LENGTH, bytes.length);
        }
        ex.getResponseSender().send(ByteBuffer.wrap(bytes));
      } else {
        String data = String.valueOf(retval);
        int length = data.getBytes().length;
        if (length < Router.MTU) {
          ex.getResponseHeaders().add(Headers.CONTENT_LENGTH, length);//workaround, for some reason undertow doesn't always set this
        }
        HeaderValues headerValues = ex.getResponseHeaders().get(Headers.CONTENT_TYPE);
        if (headerValues == null || headerValues.isEmpty()) {
          ex.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html;charset=utf-8");
        }
        ex.getResponseSender().send(data, new NoCompletionCallback());
      }
    }
  }

  protected Object renderTemplate(Object retval) throws Exception {
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
