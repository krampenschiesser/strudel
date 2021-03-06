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
package de.ks.strudel.route.handler.route;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import org.xnio.IoUtils;

import java.io.IOException;

class NoCompletionCallback implements IoCallback {
  @Override
  public void onComplete(HttpServerExchange exchange, Sender sender) {
  }

  @Override
  public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
    try {
      exchange.endExchange();
    } finally {
      IoUtils.safeClose(exchange.getConnection());
    }
  }
}
