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
package de.ks.strudel;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.PathTemplateMatch;

import java.util.Map;

public class Request {
  protected final HttpServerExchange exchange;

  protected Request() {
    exchange = null;
  }

  public Request(HttpServerExchange exchange) {
    this.exchange = exchange;
  }

  public HttpServerExchange getExchange() {
    return exchange;
  }

  public String routeParameter(String key) {
    PathTemplateMatch attachment = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
    Map<String, String> parameters = attachment.getParameters();
    return parameters.get(key);
  }

  public String routeWildcard() {
    PathTemplateMatch attachment = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
    Map<String, String> parameters = attachment.getParameters();
    return parameters.get("*");
  }

  public String path() {
    return exchange.getRequestPath();
  }
}
