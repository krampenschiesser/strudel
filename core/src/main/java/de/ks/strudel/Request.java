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
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.PathTemplateMatch;

import java.net.InetSocketAddress;
import java.util.*;

public class Request {
  final HttpServerExchange exchange;
  final Locale locale;

  protected Request() {
    exchange = null;
    locale = null;
  }

  public Request(HttpServerExchange exchange, Locale locale) {
    this.exchange = exchange;
    this.locale = locale;
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

  public String protocol() {
    return exchange.getProtocol().toString();
  }

  public String method() {
    return exchange.getRequestMethod().toString();
  }

  public String uri() {
    return exchange.getRequestURI();
  }

  public String relativePath() {
    return exchange.getRelativePath();
  }

  public String resolvedPath() {
    return exchange.getResolvedPath();
  }

  public String queryString() {
    return exchange.getQueryString();
  }

  public String url() {
    return exchange.getRequestURL();
  }

  public String charset() {
    return exchange.getRequestCharset();
  }

  public String host() {
    return exchange.getHostName();
  }

  public int port() {
    return exchange.getHostPort();
  }

  public InetSocketAddress sourceAddress() {
    return exchange.getSourceAddress();
  }

  public String headerValue(String key) {
    return Optional.ofNullable(header(key)).map(HeaderValues::peekFirst).orElse(null);
  }

  public HeaderValues header(String key) {
    return headers().get(key);
  }

  public HeaderMap headers() {
    return exchange.getRequestHeaders();
  }

  public String queryParameterFirst(String key) {
    return queryParameter(key).peekFirst();
  }

  public Deque<String> queryParameter(String key) {
    return queryParameters().getOrDefault(key, new ArrayDeque<>(1));
  }

  public Map<String, Deque<String>> queryParameters() {
    return exchange.getQueryParameters();
  }

  public String pathParameterFirst(String key) {
    return pathParameter(key).peekFirst();
  }

  public Deque<String> pathParameter(String key) {
    return pathParameters().getOrDefault(key, new ArrayDeque<>(1));
  }

  public Map<String, Deque<String>> pathParameters() {
    return exchange.getPathParameters();
  }

  public String cookieValue(String key) {
    return Optional.ofNullable(cookie(key)).map(Cookie::getValue).orElse(null);
  }

  public Cookie cookie(String key) {
    return requestCookies().get(key);
  }

  public Map<String, Cookie> requestCookies() {
    return exchange.getRequestCookies();
  }

  public long requestStartTime() {
    return exchange.getRequestStartTime();
  }

  public Locale getLocale() {
    return locale;
  }
}
