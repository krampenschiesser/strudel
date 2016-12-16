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

/**
 * Wrapper and utility around the {@link HttpServerExchange}
 */
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

  /**
   * @param status sets given status for response
   * @return self
   */
  public Response status(HttpStatus status) {
    return status(status.getValue());
  }

  /**
   * @param code sets given status for response
   * @return self
   */
  public Response status(int code) {
    exchange.setStatusCode(code);
    return this;
  }

  /**
   * Stops execution immediately throwing a halt exception with the given status
   *
   * @param status to use as holt reason
   */
  public void halt(HttpStatus status) {
    halt(status.getValue());
  }

  /**
   * Stops execution immediately throwing a halt exception with the given status
   *
   * @param status to use as holt reason
   */
  public void halt(int status) {
    throw new HaltException(status);
  }

  /**
   * redirects the response to the target with status Moved_Temporarily 302
   *
   * @param target to redirect to
   * @return self
   */
  public Response redirect(String target) {
    return redirect(target, HttpStatus.MOVED_TEMPORARILY);
  }

  /**
   * redirects the response to the target with given status
   *
   * @param target to redirect to
   * @param status to give as answer
   * @return self
   */
  public Response redirect(String target, HttpStatus status) {
    return redirect(target, status.getValue());
  }

  /**
   * redirects the response to the target with given status
   *
   * @param target to redirect to
   * @param status to give as answer
   * @return self
   */
  public Response redirect(String target, int status) {
    exchange.setStatusCode(status);
    exchange.getResponseHeaders().add(Headers.LOCATION, target);
    return this;
  }

  /**
   * @return response charset
   */
  public String charset() {
    return exchange.getResponseCharset();
  }

  /**
   * @return amount of bytes already sent
   */
  public long bytesSent() {
    return exchange.getResponseBytesSent();
  }

  /**
   * sets a new header value
   *
   * @param headerName  name
   * @param headerValue value
   * @return self
   */
  public Response header(String headerName, String headerValue) {
    exchange.getResponseHeaders().add(new HttpString(headerName), headerValue);
    return this;
  }

  /**
   * Get all response headers
   * @return manipulatable map
   */
  public HeaderMap headers() {
    return exchange.getResponseHeaders();
  }

  /**
   * Sets response content type header
   * @param type content type
   * @return self
   */
  public Response contentType(String type) {
    exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, type);
    return this;
  }

  /**
   * @return response content length
   */
  public long contentLength() {
    return exchange.getResponseContentLength();
  }

  /**
   * sets response content length
   * @param length  l
   * @return self
   */
  public Response contentLength(long length) {
    exchange.setResponseContentLength(length);
    return this;
  }

  /**
   * Sets a response cookie
   * @param key cookie name
   * @param value val
   * @return self
   */
  public Response cookie(String key, String value) {
    exchange.setResponseCookie(new CookieImpl(key, value));
    return this;
  }

  /**
   * Sets a response cookie
   *
   * @param cookie c
   * @return self
   */
  public Response cookie(Cookie cookie) {
    exchange.setResponseCookie(cookie);
    return this;
  }

  /**
   * @return a map of all response cookies
   */
  public Map<String, Cookie> cookies() {
    return exchange.getResponseCookies();
  }

  /**
   * @return the response cookie for the given name
   */
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
