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

import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Request;
import de.ks.strudel.Response;
import io.undertow.server.HttpServerExchange;

import javax.annotation.Nullable;
import javax.inject.Provider;

public class AsyncCallbackHandler extends WrappingHandler {
  private final HandlerNoReturn asyncBefore;
  private final HandlerNoReturn asyncAfter;
  private final Provider<Request> request;
  private final Provider<Response> response;

  public AsyncCallbackHandler(@Nullable HandlerNoReturn asyncBefore, @Nullable HandlerNoReturn asyncAfter, Provider<Request> request, Provider<Response> response) {
    this.asyncBefore = asyncBefore;
    this.asyncAfter = asyncAfter;
    this.request = request;
    this.response = response;
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
