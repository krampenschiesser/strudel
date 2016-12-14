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

import io.undertow.server.HttpServerExchange;

public class EndExchangeHandler extends WrappingHandler {
  private final ThreadLocal<Boolean> asyncRoute;

  public EndExchangeHandler(ThreadLocal<Boolean> asyncRoute) {
    this.asyncRoute = asyncRoute;
  }

  @Override
  protected boolean before(HttpServerExchange exchange) throws Exception {
    return true;
  }

  @Override
  protected void after(HttpServerExchange exchange) throws Exception {
    if (!asyncRoute.get()) {
      boolean inProgress = (exchange.isResponseStarted() || exchange.isDispatched());
      if (!inProgress && exchange.getStatusCode() == 200) {
        exchange.setStatusCode(HttpStatus.NOT_FOUND.getValue());
      }
      if (!exchange.isComplete() && !inProgress) {
        exchange.endExchange();
      }
    }
  }
}
