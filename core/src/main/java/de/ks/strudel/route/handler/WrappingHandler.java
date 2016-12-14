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
package de.ks.strudel.route.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public abstract class WrappingHandler implements HttpHandler {
  private HttpHandler next;

  public WrappingHandler setNext(HttpHandler next) {
    this.next = next;
    return this;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    boolean continueExecution = before(exchange);
    try {
      if (continueExecution) {
        next.handleRequest(exchange);
      }
    } catch (Exception e) {
      handleException(e, exchange);
    } finally {
      after(exchange);
    }
  }

  protected void handleException(Exception e, HttpServerExchange exchange) throws Exception {
    throw e;
  }

  /**
   * @param exchange
   * @return true if execution should continue and call #next
   */
  protected abstract boolean before(HttpServerExchange exchange) throws Exception;

  protected abstract void after(HttpServerExchange exchange) throws Exception;
}
