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

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class RequestFormParser {
  final AtomicReference<FormData> formData = new AtomicReference<>();
  HttpServerExchange exchange;

  public RequestFormParser setExchange(HttpServerExchange exchange) {
    this.exchange = exchange;
    return this;
  }

  public String formData(String key) throws IOException {
    FormData data = formData();
    FormData.FormValue value = data.getFirst(key);
    return value == null ? null : value.getValue();
  }

  public Path formDataFile(String key) throws IOException {
    FormData data = formData();
    FormData.FormValue value = data.getFirst(key);
    if (value == null || !value.isFile()) {
      return null;
    } else {
      return value.getPath();
    }
  }

  public FormData formData() throws IOException {
    if (formData.get() != null) {
      return formData.get();
    }
    exchange.startBlocking();
    FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
    if (parser != null) {
      FormData data = parser.parseBlocking();
      formData.set(data);
      return formData.get();
    } else {
      return null;
    }
  }

}
