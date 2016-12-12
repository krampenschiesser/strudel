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
import io.undertow.util.Headers;

public class Response {
  protected HttpServerExchange exchange;

  public Response(HttpServerExchange exchange) {
    this.exchange = exchange;
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

  public void redirect(String target) {
    redirect(target, HttpStatus.MOVED_TEMPORARILY);
  }

  public void redirect(String target, HttpStatus status) {
    redirect(target, status.getValue());
  }

  public void redirect(String target, int status) {
    exchange.setStatusCode(status);
    exchange.getResponseHeaders().add(Headers.LOCATION, target);
  }
}
