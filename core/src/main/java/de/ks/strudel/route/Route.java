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
import io.undertow.util.HttpString;
import io.undertow.websockets.WebSocketConnectionCallback;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Immutable descriptor of a route.
 */
public class Route {
  private final Supplier<Handler> handler;
  private final HttpMethod method;
  private final String path;
  private final boolean gzip;
  private final boolean async;
  private final FilterType filterType;
  private final HandlerNoReturn asyncBefore, asyncAfter;
  private final Class<? extends TemplateEngine> templateEngine;
  private final boolean etag;
  private final Class<? extends JsonParser> jsonParser;
  private final WebSocketConnectionCallback webSocketConnectionCallback;

  public Route(RouteBuilder routeBuilder) {
    handler = routeBuilder.handler;
    method = routeBuilder.method;
    path = routeBuilder.path;
    gzip = routeBuilder.gzip;
    filterType = routeBuilder.filterType;
    async = routeBuilder.async;
    asyncBefore = routeBuilder.asyncBefore;
    asyncAfter = routeBuilder.asyncAfter;
    templateEngine = routeBuilder.engineClass;
    webSocketConnectionCallback = routeBuilder.webSocketConnectionCallback;
    etag = routeBuilder.etag;
    jsonParser = routeBuilder.jsonParser;
  }

  public Handler getHandler() {
    return handler.get();
  }

  public HttpMethod getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public boolean isGzip() {
    return gzip;
  }

  public boolean isAsync() {
    return async;
  }

  public boolean isFilter() {
    return filterType != null;
  }

  public FilterType getFilterType() {
    return filterType;
  }

  public HandlerNoReturn getAsyncBefore() {
    return asyncBefore;
  }

  public HandlerNoReturn getAsyncAfter() {
    return asyncAfter;
  }

  public Class<? extends TemplateEngine> getTemplateEngine() {
    return templateEngine;
  }

  public List<HttpString> getMethods() {
    if (method == HttpMethod.ALL) {
      return Arrays.asList(//
        HttpMethod.GET.getMethod(),//
        HttpMethod.PUT.getMethod(),//
        HttpMethod.POST.getMethod(),//
        HttpMethod.DELETE.getMethod()//
      );
    } else {
      return Collections.singletonList(method.getMethod());
    }
  }

  public boolean isWebsocket() {
    return webSocketConnectionCallback != null;
  }

  public boolean isEtag() {
    return etag;
  }

  public WebSocketConnectionCallback getWebSocketConnectionCallback() {
    return webSocketConnectionCallback;
  }

  public boolean isParseAsJson() {
    return jsonParser != null;
  }

  public Class<? extends JsonParser> getJsonParser() {
    return jsonParser;
  }
}
