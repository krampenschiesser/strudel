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
package de.ks.strudel.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.strudel.Strudel;
import de.ks.strudel.StrudelModule;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.function.Executable;

public class StrudelTestExtension implements Extension, BeforeEachCallback, AfterEachCallback {
  private Strudel strudel;
  public static int port = 7777;

  @Override
  public void afterEach(TestExtensionContext context) throws Exception {
    strudel.stop();
  }

  @Override
  public void beforeEach(TestExtensionContext context) throws Exception {
    Injector injector = Guice.createInjector(new StrudelModule());
    strudel = injector.getInstance(Strudel.class);
    strudel.options().port(port);
    RestAssured.port = port;
    RestAssured.defaultParser = Parser.HTML;

    injector.injectMembers(context.getTestInstance());
  }

  public static Executable wrap(Object test, Executable original) {
    Injector injector = Guice.createInjector(new StrudelModule());
    Strudel local = injector.getInstance(Strudel.class);
    local.options().port(StrudelTestExtension.port);
    RestAssured.port = StrudelTestExtension.port;
    RestAssured.defaultParser = Parser.HTML;

    injector.injectMembers(test);
    return () -> {
      try {
        original.execute();
      } finally {
        local.stop();
      }
    };
  }
}
