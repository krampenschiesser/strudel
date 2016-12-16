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

import de.ks.strudel.Response;
import de.ks.strudel.localization.LocaleResolver;
import de.ks.strudel.request.Request;
import de.ks.strudel.request.RequestBodyParser;
import de.ks.strudel.request.RequestFormParser;
import de.ks.strudel.scope.RequestScope;
import io.undertow.server.HttpServerExchange;

import java.util.Locale;

/**
 * Starts and stops the request scope.
 */
public class RequestScopeHandler extends WrappingHandler {
  private final RequestScope requestScope;
  private final LocaleResolver localeResolver;
  private final RequestBodyParser bodyParser;
  private final RequestFormParser formParser;

  public RequestScopeHandler(RequestScope requestScope, LocaleResolver localeResolver, RequestBodyParser bodyParser, RequestFormParser formParser) {
    this.requestScope = requestScope;
    this.localeResolver = localeResolver;
    this.bodyParser = bodyParser;
    this.formParser = formParser;
  }

  @Override protected boolean before(HttpServerExchange ex) {
    Locale locale = localeResolver.getLocale(ex);
    Request request = new Request(ex, locale, bodyParser, formParser);
    Response response = new Response(ex);
    requestScope.enter(request, response, locale);
    return true;
  }

  @Override protected void after(HttpServerExchange exchange) {
    requestScope.exit();
  }
}
