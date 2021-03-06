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
package de.ks.strudel.localization;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.Headers;

import java.util.Deque;
import java.util.Locale;

/**
 * Resolves the locale to use for a request.
 * See below at {@link #getLocale(HttpServerExchange)}
 */
public class LocaleResolver {
  /**
   * Parses the locale from the request.
   * 1. by query parameter: /?lang=de
   * 2. by cookie: lang=de
   * 3. by http header accept_locale
   *
   * @param exchange ex
   * @return the resolved locale, default is english
   */
  public Locale getLocale(HttpServerExchange exchange) {
    Locale byParameter = getLocaleFromParameter(exchange);
    if (byParameter != null) {
      return byParameter;
    }
    Locale byCookie = getLangByCookie(exchange);
    if (byCookie != null) {
      return byCookie;
    }
    Locale byHeader = getHttpHeaderLocale(exchange);
    if (byHeader != null) {
      return byHeader;
    } else {
      return Locale.ENGLISH;
    }
  }

  protected Locale getLocaleFromParameter(HttpServerExchange exchange) {
    Deque<String> lang = exchange.getQueryParameters().get("lang");
    if (lang != null && !lang.isEmpty()) {
      String langCode = lang.iterator().next();
      Locale locale = Locale.forLanguageTag(langCode);
      return locale;
    }
    return null;
  }

  protected Locale getLangByCookie(HttpServerExchange exchange) {
    Cookie cookie = exchange.getRequestCookies().get("lang");
    if (cookie != null) {
      String langCode = cookie.getValue();
      Locale locale = Locale.forLanguageTag(langCode);
      return locale;
    }
    return null;
  }

  protected Locale getHttpHeaderLocale(HttpServerExchange exchange) {
    String acceptLanguage = exchange.getRequestHeaders().getFirst(Headers.ACCEPT_LANGUAGE);
    if (acceptLanguage != null) {
      int endMainLang = acceptLanguage.indexOf(",");
      if (endMainLang > 0) {
        acceptLanguage = acceptLanguage.substring(0, endMainLang);
      }
      Locale locale = Locale.forLanguageTag(acceptLanguage);
      return locale;
    }
    return null;
  }
}
