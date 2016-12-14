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

import io.undertow.server.HttpServerExchange;

public class AsyncRouteHandler extends WrappingHandler {
  private final ThreadLocal<Boolean> asyncRoute;

  public AsyncRouteHandler(ThreadLocal<Boolean> asyncRoute) {
    this.asyncRoute = asyncRoute;
  }

  @Override
  protected boolean before(HttpServerExchange exchange) throws Exception {
    asyncRoute.set(false);
    return true;
  }

  @Override
  protected void after(HttpServerExchange exchange) throws Exception {
    if (asyncRoute.get()) {
      asyncRoute.set(false);
    }
  }
}
