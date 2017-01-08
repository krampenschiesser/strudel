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
package de.ks.strudel.request;

import de.ks.strudel.json.JsonParser;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.form.FormData;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.*;

/**
 * Wrapper and helper around undertows HttpServerExchange
 */
public class Request {
  final HttpServerExchange exchange;
  final Locale locale;
  final RequestBodyParser bodyParser;
  final RequestFormParser formParser;

  protected Request() {
    bodyParser = null;
    formParser = null;
    exchange = null;
    locale = null;
  }

  public Request(HttpServerExchange exchange, Locale locale, RequestBodyParser bodyParser, RequestFormParser formParser) {
    this.exchange = exchange;
    this.locale = locale;
    this.bodyParser = bodyParser;
    this.formParser = formParser;
  }

  public HttpServerExchange getExchange() {
    return exchange;
  }

  public @Nullable String routeParameter(String key) {
    PathTemplateMatch attachment = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
    Map<String, String> parameters = attachment.getParameters();
    return parameters.get(key);
  }

  public @Nullable String routeWildcard() {
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

  /**
   * @return address of request client
   */
  public InetSocketAddress clientAddress() {
    return exchange.getSourceAddress();
  }

  public @Nullable String headerValue(String key) {
    return Optional.ofNullable(header(key)).map(HeaderValues::peekFirst).orElse(null);
  }

  public HeaderValues header(String key) {
    return headers().get(key);
  }

  public HeaderMap headers() {
    return exchange.getRequestHeaders();
  }

  public
  @Nullable
  String queryParameterValue(String key) {
    return queryParameter(key).peekFirst();
  }

  public Deque<String> queryParameter(String key) {
    return queryParameters().getOrDefault(key, new ArrayDeque<>(1));
  }

  public Map<String, Deque<String>> queryParameters() {
    return exchange.getQueryParameters();
  }

  /**
   * @param key path param
   * @return null
   */
  public
  @Nullable
  String pathParameterValue(String key) {
    return pathParameter(key).peekFirst();
  }

  /**
   * @param key path param
   * @return empty queue or filled
   */
  public Deque<String> pathParameter(String key) {
    return pathParameters().getOrDefault(key, new ArrayDeque<>(1));
  }

  public Map<String, Deque<String>> pathParameters() {
    return exchange.getPathParameters();
  }

  /**
   * @param key of the cookie
   * @return null or the value of the cookie identified by key
   */
  public @Nullable String cookieValue(String key) {
    return Optional.ofNullable(cookie(key)).map(Cookie::getValue).orElse(null);
  }

  public @Nullable Cookie cookie(String key) {
    return requestCookies().get(key);
  }

  public Map<String, Cookie> requestCookies() {
    return exchange.getRequestCookies();
  }

  /**
   * @return start time
   */
  public long requestStartTime() {
    return exchange.getRequestStartTime();
  }

  /**
   * @return request content length
   */
  public long contentLength() {
    return exchange.getRequestContentLength();
  }

  /**
   * @return the locale resolved for this request. See @{@link de.ks.strudel.localization.LocaleResolver}
   */
  public Locale getLocale() {
    return locale;
  }

  /**
   * Reads the body from the input stream.
   *
   * @return the bytes[] associated with the read
   * @throws IOException
   */
  public byte[] bodyAsBytes() throws IOException {
    return bodyParser.bodyAsBytes();
  }

  /**
   * Reads the body from the input stream,
   * then parses the body with the default Parser bound the interface JsonParser
   * to an object of type T which is returned
   *
   * @return an instance of T
   * @throws Exception from input stream and json parser
   */
  public <T> T bodyFromJson(Class<T> clazz) throws Exception {
    return bodyParser.bodyFromJson(clazz);
  }

  /**
   * Reads the body from the input stream,
   * then parses the body with the given JsonParser
   * to an object of type T which is returned
   *
   * @return an instance of T
   * @throws Exception from input stream and json parser
   */
  public <T> T bodyFromJson(Class<T> clazz, Class<? extends JsonParser> parser) throws Exception {
    return bodyParser.bodyFromJson(clazz, parser);
  }

  /**
   * Reads the body from the input stream.
   * Conversion to string is done either via the request charset, or if
   * not available via UTF-8
   *
   * @return the body
   * @throws IOException from input stream
   */
  public String body() throws IOException {
    return bodyParser.body();
  }

  /**
   * Reads the formdata from the input stream and extracts the given value.
   *
   * @param key of the formdata
   * @return null or value
   * @throws IOException from input stream
   */
  public @Nullable String formData(String key) throws IOException {
    return formParser.formData(key);
  }

  /**
   * Reads the formdata from the input stream and extracts the given value.
   *
   * @param key of the formdata
   * @return null or a path pointing to a file in the java.io.tmpdir
   * @throws IOException from input stream
   */
  public @Nullable Path formDataFile(String key) throws IOException {
    return formParser.formDataFile(key);
  }

  /**
   * Reads the formdata from the input stream
   *
   * @return null or value
   * @throws IOException from input stream
   */
  public @Nullable FormData formData() throws IOException {
    return formParser.formData();
  }

  public String userAgent() {
    return headerValue(Headers.USER_AGENT_STRING);
  }
}
