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

import de.ks.strudel.Response;
import de.ks.strudel.localization.LocaleResolver;
import de.ks.strudel.request.Request;
import de.ks.strudel.request.RequestBodyParser;
import de.ks.strudel.request.RequestFormParser;
import de.ks.strudel.route.handler.WrappingHandler;
import de.ks.strudel.scope.RequestScope;
import io.undertow.server.HttpServerExchange;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.util.Locale;

/**
 * Starts and stops the request scope.
 */
@NotThreadSafe//new instance per route handling
public class RequestScopeHandler extends WrappingHandler {
  protected final RequestScope requestScope;
  protected final LocaleResolver localeResolver;
  protected RequestBodyParser bodyParser;
  protected RequestFormParser formParser;

  @Inject
  public RequestScopeHandler(RequestScope requestScope, LocaleResolver localeResolver) {
    this.requestScope = requestScope;
    this.localeResolver = localeResolver;
  }

  public RequestScopeHandler setBodyParser(RequestBodyParser bodyParser) {
    this.bodyParser = bodyParser;
    return this;
  }

  public RequestScopeHandler setFormParser(RequestFormParser formParser) {
    this.formParser = formParser;
    return this;
  }

  @Override
  protected boolean before(HttpServerExchange ex) {
    Locale locale = localeResolver.getLocale(ex);
    Request request = new Request(ex, locale, bodyParser, formParser);
    Response response = new Response(ex);
    requestScope.enter(request, response, locale);
    return true;
  }

  @Override
  protected void after(HttpServerExchange exchange) {
    requestScope.exit();
  }
}
