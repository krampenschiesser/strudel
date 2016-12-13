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

import de.ks.strudel.HaltException;
import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Request;
import de.ks.strudel.Response;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

class MainHandler implements HttpHandler {
  private final Router router;

  public MainHandler(Router router) {
    this.router = router;
  }

  @Override
  public void handleRequest(HttpServerExchange exchange) throws Exception {
    try {
      router.asyncRoute.set(false);
      try {
        router.before.handleRequest(exchange);
        if (!exchange.isComplete()) {
          router.routing.handleRequest(exchange);
        }
      } catch (HaltException e) {
        throw e;
      } catch (Exception e) {
        HandlerNoReturn handler = Optional.ofNullable(router.exceptionMappings.get(e.getClass())).map(Supplier::get).orElse(null);
        if (handler == null) {
          handler = router.exceptionMappings.entrySet().stream()//
                                            .filter(entry -> entry.getKey().isAssignableFrom(e.getClass()))//
                                            .map(Map.Entry::getValue).map(Supplier::get)//
                                            .findFirst().orElse(null);
        }
        if (handler != null) {
          handler.handle(new Request(exchange), new Response(exchange));
          if (!exchange.isResponseStarted() && exchange.getStatusCode() == 200) {
            exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.getValue());
          }
        } else {
          throw e;
        }
      }
      if (router.asyncRoute.get()) {
        router.asyncRoute.set(false);
      } else {
        router.after.handleRequest(exchange);

        boolean inProgress = (exchange.isResponseStarted() || exchange.isDispatched());
        if (!inProgress && exchange.getStatusCode() == 200) {
          exchange.setStatusCode(HttpStatus.NOT_FOUND.getValue());
        }
        if (!exchange.isComplete() && !inProgress) {
          exchange.endExchange();
        }
      }
    } catch (HaltException e) {
      exchange.setStatusCode(e.getStatus());
      exchange.endExchange();
    }
  }
}
