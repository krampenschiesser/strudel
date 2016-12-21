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

import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Response;
import de.ks.strudel.request.Request;
import de.ks.strudel.route.handler.WrappingHandler;
import io.undertow.server.HttpServerExchange;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Used to execute async before/after callbacks around a handler
 */
@NotThreadSafe//new instance per handling
public class AsyncCallbackHandler extends WrappingHandler {
  protected HandlerNoReturn asyncBefore;
  protected HandlerNoReturn asyncAfter;
  protected final Provider<Request> request;
  protected final Provider<Response> response;

  @Inject
  public AsyncCallbackHandler(Provider<Request> request, Provider<Response> response) {
    this.request = request;
    this.response = response;
  }

  public AsyncCallbackHandler setAsyncAfter(HandlerNoReturn asyncAfter) {
    this.asyncAfter = asyncAfter;
    return this;
  }

  public AsyncCallbackHandler setAsyncBefore(HandlerNoReturn asyncBefore) {
    this.asyncBefore = asyncBefore;
    return this;
  }

  @Override
  protected boolean before(HttpServerExchange exchange) throws Exception {
    if (asyncBefore != null) {
      asyncBefore.handle(request.get(), response.get());
    }
    return true;
  }

  @Override
  protected void after(HttpServerExchange exchange) throws Exception {
    if (asyncAfter != null) {
      asyncAfter.handle(request.get(), response.get());
    }
  }
}
