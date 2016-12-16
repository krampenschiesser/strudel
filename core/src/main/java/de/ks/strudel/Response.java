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

import de.ks.strudel.route.HttpStatus;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.Map;

public class Response {
  protected final HttpServerExchange exchange;

  protected Response() {
    exchange = null;
  }

  public Response(HttpServerExchange exchange) {
    this.exchange = exchange;
  }

  public HttpServerExchange getExchange() {
    return exchange;
  }

  public Response status(HttpStatus status) {
    return status(status.getValue());
  }

  public Response status(int code) {
    exchange.setStatusCode(code);
    return this;
  }

  public void halt(HttpStatus status) {
    halt(status.getValue());
  }

  public void halt(int status) {
    throw new HaltException(status);
  }

  public Response redirect(String target) {
    return redirect(target, HttpStatus.MOVED_TEMPORARILY);
  }

  public Response redirect(String target, HttpStatus status) {
    return redirect(target, status.getValue());
  }

  public Response redirect(String target, int status) {
    exchange.setStatusCode(status);
    exchange.getResponseHeaders().add(Headers.LOCATION, target);
    return this;
  }

  public String charset() {
    return exchange.getResponseCharset();
  }

  public long bytesSent() {
    return exchange.getResponseBytesSent();
  }

  public Response header(String headerName, String headerValue) {
    exchange.getResponseHeaders().add(new HttpString(headerName), headerValue);
    return this;
  }

  public HeaderMap headers() {
    return exchange.getResponseHeaders();
  }

  public Response contentType(String type) {
    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, type);
    return this;
  }

  public long contentLength() {
    return exchange.getResponseContentLength();
  }

  public Response contentLength(long length) {
    exchange.setResponseContentLength(length);
    return this;
  }

  public Response cookie(String key, String value) {
    exchange.setResponseCookie(new CookieImpl(key, value));
    return this;
  }

  public Response cookie(Cookie cookie) {
    exchange.setResponseCookie(cookie);
    return this;
  }

  public Map<String, Cookie> cookies() {
    return exchange.getResponseCookies();
  }

  public Cookie cookie(String key) {
    return exchange.getResponseCookies().get(key);
  }

  public boolean isResponseStarted() {
    return exchange.isResponseStarted();
  }

  public boolean isResponseComplete() {
    return exchange.isResponseComplete();
  }

  public boolean isResponseChannelAvailable() {
    return exchange.isResponseChannelAvailable();
  }

}
