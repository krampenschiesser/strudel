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

import de.ks.strudel.Handler;
import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.json.JsonParser;
import de.ks.strudel.template.TemplateEngine;
import io.undertow.websockets.WebSocketConnectionCallback;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Route builder/configurator
 */
public class RouteBuilder {
  String path;
  Supplier<Handler> handler;
  HttpMethod method;
  boolean gzip;
  FilterType filterType;
  boolean async;
  HandlerNoReturn asyncBefore, asyncAfter;
  Class<? extends TemplateEngine> engineClass;
  WebSocketConnectionCallback webSocketConnectionCallback;
  boolean etag;
  Class<? extends JsonParser> jsonParser;

  /**
   * @param path the path where the route should be bound
   * @return self
   */
  public RouteBuilder path(String path) {
    this.path = path;
    return this;
  }

  /**
   * @param handler a supplier for the handler that is executed when the route is called by a client.
   *                A supplier because it allows us to use stateless handlers that are instantiated anew with each call.
   * @return self
   */
  public RouteBuilder handler(Supplier<Handler> handler) {
    this.handler = handler;
    return this;
  }

  public RouteBuilder method(HttpMethod method) {
    this.method = method;
    return this;
  }

  public RouteBuilder get() {
    return method(HttpMethod.GET);
  }

  public RouteBuilder put() {
    return method(HttpMethod.PUT);
  }

  public RouteBuilder post() {
    return method(HttpMethod.POST);
  }

  public RouteBuilder delete() {
    return method(HttpMethod.DELETE);
  }

  /**
   * marks this route to be executed async in a worker thread
   *
   * @return self
   */
  public RouteBuilder async() {
    return async(null, null);
  }

  /**
   * marks this route to be executed async in a worker thread
   * @param before callback executed before main handler
   * @param after callback executed before main handler
   * @return self
   */
  public RouteBuilder async(@Nullable HandlerNoReturn before, @Nullable HandlerNoReturn after) {
    this.async = true;
    this.asyncBefore = before;
    this.asyncAfter = after;
    return this;
  }

  /**
   * marks this route to be executed in the IO thread
   * @return self
   */
  public RouteBuilder sync() {
    this.async = false;
    asyncAfter = null;
    asyncBefore = null;
    return this;
  }

  /**
   * content returned by this route is zipped. please not that only content above {@link Router#MTU} is zipped
   * @return self
   */
  public RouteBuilder gzip() {
    this.gzip = true;
    return this;
  }

  /**
   * Marks this route as a filter that is either executed before/after the other handlers
   * @param filterType before/after
   * @return self
   */
  public RouteBuilder filter(FilterType filterType) {
    this.filterType = filterType;
    return this;
  }

  /**
   * Marks this route to be rendered as a template with the default template engine bound to the interfac TemplateEngine.
   * @return self
   */
  public RouteBuilder template() {
    return template(TemplateEngine.class);
  }

  /**
   * Marks this route to be rendered as a template with the give template engine.
   * @param engineClass the template engine to use to render this route
   * @return self
   */
  public RouteBuilder template(Class<? extends TemplateEngine> engineClass) {
    this.engineClass = engineClass;
    async();
    gzip();
    return this;
  }

  /**
   * Marks this route to serialize the result of a handler as json string
   * using the default parser bound to the interface JsonParser
   * @return self
   */
  public RouteBuilder json() {
    return json(JsonParser.class);
  }

  /**
   * Marks this route to serialize the result of a handler as json string
   * using the given parser
   * @return self
   */
  public RouteBuilder json(Class<? extends JsonParser> parserClass) {
    this.jsonParser = parserClass;
    return this;
  }

  //  public RouteBuilder cacheEtag() {
//    this.etag=true;
//    return this;
//  }
  public Route build() {
    if (filterType == FilterType.BEFORE && async) {
      throw new IllegalStateException("Before-filters are not allowed to be asynchronous!");
    }
    return new Route(this);
  }

  RouteBuilder websocket(WebSocketConnectionCallback webSocketConnectionCallback) {
    this.webSocketConnectionCallback = webSocketConnectionCallback;
    return this;
  }

}
