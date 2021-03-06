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
package de.ks.strudel.route.handler.main;

import de.ks.strudel.route.handler.AsyncTracker;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;

/**
 * Executes before/after filters
 */
public class BeforeAfterMainHandler implements HttpHandler {
  protected final BeforeHandler before;
  protected final MainRoutingHandler main;
  protected final AfterHandler after;
  protected final AsyncTracker asyncTracker;

  @Inject
  public BeforeAfterMainHandler(BeforeHandler before, MainRoutingHandler main, AfterHandler after, AsyncTracker asyncTracker) {
    this.before = before;
    this.main = main;
    this.after = after;
    this.asyncTracker = asyncTracker;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    before.handleRequest(exchange);
    if (!exchange.isComplete()) {
      main.handleRequest(exchange);
      if (!asyncTracker.isAsyncRoute()) {
        after.handleRequest(exchange);
      }
    }
  }
}
