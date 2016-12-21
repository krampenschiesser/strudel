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
import de.ks.strudel.json.JsonResolver;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class RequestBodyParser {
  HttpServerExchange exchange;
  final JsonResolver jsonResolver;
  Class<? extends JsonParser> preferredParser;
  final AtomicReference<String> body = new AtomicReference<>();
  final AtomicReference<byte[]> bodyBytes = new AtomicReference<>();

  @Inject
  public RequestBodyParser(JsonResolver jsonResolver) {
    this.exchange = exchange;
    this.jsonResolver = jsonResolver;
    this.preferredParser = preferredParser;
  }

  public RequestBodyParser setExchange(HttpServerExchange exchange) {
    this.exchange = exchange;
    return this;
  }

  public RequestBodyParser setPreferredParser(Class<? extends JsonParser> preferredParser) {
    this.preferredParser = preferredParser;
    return this;
  }

  public byte[] bodyAsBytes() throws IOException {
    if (bodyBytes.get() != null) {
      return bodyBytes.get();
    }
    exchange.startBlocking();
    try (InputStream inputStream = exchange.getInputStream()) {
      ByteArrayOutputStream bos = readToOutputStream(inputStream);
      bodyBytes.set(bos.toByteArray());
      return bodyBytes.get();
    }
  }

  public <T> T bodyFromJson(Class<T> clazz) throws Exception {
    return bodyFromJson(clazz, preferredParser != null ? preferredParser : JsonParser.class);
  }

  public <T> T bodyFromJson(Class<T> clazz, Class<? extends JsonParser> parser) throws Exception {
    if (jsonResolver == null) {
      throw new IllegalStateException("You are in error handling, no parser available to parse the request body");
    }
    String body = body();
    JsonParser jsonParser = jsonResolver.getJsonParser(parser);
    T retval = jsonParser.fromString(body, clazz);
    return retval;
  }

  public String body() throws IOException {
    if (body.get() != null) {
      return body.get();
    }
    exchange.startBlocking();
    try (InputStream inputStream = exchange.getInputStream()) {
      ByteArrayOutputStream bos = readToOutputStream(inputStream);
      String utf8 = StandardCharsets.UTF_8.displayName();
      String charset = Optional.of(exchange.getRequestCharset()).orElse(utf8);
      try {
        this.body.set(bos.toString(charset));
        return body.get();
      } catch (UnsupportedEncodingException e) {
        return bos.toString(utf8);
      }
    }
  }

  private ByteArrayOutputStream readToOutputStream(InputStream inputStream) throws IOException {
    int size = exchange.getRequestContentLength() > 0 ? (int) exchange.getRequestContentLength() : 1024;
    ByteArrayOutputStream bos = new ByteArrayOutputStream(size);

    try (PooledByteBuffer pooled = exchange.getConnection().getByteBufferPool().getArrayBackedPool().allocate()) {
      ByteBuffer buf = pooled.getBuffer();
      while (true) {
        buf.clear();
        int c = inputStream.read(buf.array(), buf.arrayOffset(), buf.remaining());
        if (c == -1) {
          break;
        } else if (c != 0) {
          buf.limit(c);
          bos.write(buf.array(), buf.arrayOffset(), buf.limit());
        }
      }
    }
    return bos;
  }
}
