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

import de.ks.strudel.Strudel;
import de.ks.strudel.util.StrudelTestExtension;
import io.restassured.RestAssured;
import io.undertow.util.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(StrudelTestExtension.class)
public class LocaleResolverIntegrationTest {
  @Inject
  Strudel strudel;
  @Inject
  Provider<Locale> localeProvider;

  @Test
  void localeIntegration() {
    strudel.get("/", (request, response) -> localeProvider == null ? null : localeProvider.get().getLanguage());
    strudel.start();

    Locale expected = Locale.GERMAN;

    Locale locale = Locale.forLanguageTag(RestAssured.get("/?lang=de").body().asString());
    assertEquals(expected, locale);

    locale = Locale.forLanguageTag(RestAssured.given().cookie("lang", "de").get("/").body().asString());
    assertEquals(expected, locale);

    locale = Locale.forLanguageTag(RestAssured.given().header(Headers.ACCEPT_LANGUAGE_STRING, "de,en;q=0.8,de;q=0.6").get("/").body().asString());
    assertEquals(expected, locale);

    locale = Locale.forLanguageTag(RestAssured.get("/").body().asString());
    assertEquals(Locale.ENGLISH, locale);
  }
}
