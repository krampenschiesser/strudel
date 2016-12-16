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

  public RouteBuilder path(String path) {
    this.path = path;
    return this;
  }

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

  public RouteBuilder async() {
    return async(null, null);
  }

  public RouteBuilder async(@Nullable HandlerNoReturn before, @Nullable HandlerNoReturn after) {
    this.async = true;
    this.asyncBefore = before;
    this.asyncAfter = after;
    return this;
  }

  public RouteBuilder sync() {
    this.async = false;
    asyncAfter = null;
    asyncBefore = null;
    return this;
  }

  public RouteBuilder gzip() {
    this.gzip = true;
    return this;
  }

  public RouteBuilder filter(FilterType filterType) {
    this.filterType = filterType;
    return this;
  }

  public RouteBuilder template() {
    return template(TemplateEngine.class);
  }

  public RouteBuilder template(Class<? extends TemplateEngine> engineClass) {
    this.engineClass = engineClass;
    async();
    gzip();
    return this;
  }

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
