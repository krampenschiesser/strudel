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

import de.ks.strudel.Request;
import de.ks.strudel.Response;
import de.ks.strudel.scope.RequestScope;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;

public class RequestScopeHandler extends WrappingHandler {
  private final RequestScope requestScope;

  @Inject
  public RequestScopeHandler(RequestScope requestScope) {
    this.requestScope = requestScope;
  }

  @Override
  protected boolean before(HttpServerExchange ex) {
    Request request = new Request(ex);
    Response response = new Response(ex);
    requestScope.enter(request, response);
    return true;
  }

  @Override
  protected void after(HttpServerExchange exchange) {
    requestScope.exit();
  }
}
