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

import de.ks.strudel.HaltException;
import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Response;
import de.ks.strudel.request.Request;
import de.ks.strudel.route.HttpStatus;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ExceptionHandler extends WrappingHandler {
  private final Provider<Locale> localeProvider;
  private final ExceptionMappingRegistry exceptionMappingRegistry;

  @Inject
  public ExceptionHandler(ExceptionMappingRegistry exceptionMappingRegistry, Provider<Locale> localeProvider) {
    this.exceptionMappingRegistry = exceptionMappingRegistry;
    this.localeProvider = localeProvider;
  }

  @Override
  protected boolean before(HttpServerExchange exchange) throws Exception {
    return true;
  }

  @Override
  protected void after(HttpServerExchange exchange) throws Exception {

  }

  @Override
  protected void handleException(Exception e, HttpServerExchange exchange) throws Exception {
    if (e instanceof HaltException) {
      HaltException halt = (HaltException) e;
      exchange.setStatusCode(halt.getStatus());
      exchange.endExchange();
    } else {
      handleExceptionByHandler(e, exchange);
    }
  }

  protected void handleExceptionByHandler(Exception e, HttpServerExchange exchange) throws Exception {
    HandlerNoReturn handler = Optional.ofNullable(exceptionMappingRegistry.getExceptionMappings().get(e.getClass())).map(Supplier::get).orElse(null);
    if (handler == null) {
      handler = exceptionMappingRegistry.getExceptionMappings().entrySet().stream()//
                                        .filter(entry -> entry.getKey().isAssignableFrom(e.getClass()))//
                                        .map(Map.Entry::getValue).map(Supplier::get)//
                                        .findFirst().orElse(null);
    }
    if (handler != null) {
      Locale locale = safeGetLocale();
      handler.handle(new Request(exchange, locale, null, null), new Response(exchange));
      if (!exchange.isResponseStarted() && exchange.getStatusCode() == 200) {
        exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.getValue());
      }
    } else {
      throw e;
    }
  }

  private Locale safeGetLocale() {
    try {
      return localeProvider.get();
    } catch (Exception e) {
      return Locale.ENGLISH;
    }
  }
}
