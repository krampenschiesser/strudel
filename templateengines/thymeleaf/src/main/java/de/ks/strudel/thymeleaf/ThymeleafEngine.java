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
package de.ks.strudel.thymeleaf;

import de.ks.strudel.template.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.Map;

@Singleton
public class ThymeleafEngine implements TemplateEngine {

  private final org.thymeleaf.TemplateEngine engine;
  private final Provider<Locale> localeProvider;

  @Inject
  public ThymeleafEngine(org.thymeleaf.TemplateEngine engine, Provider<Locale> localeProvider) {
    this.engine = engine;
    this.localeProvider = localeProvider;
  }

  @Override
  public String render(Object model, String view) {
    Context context = new Context();
    context.setLocale(localeProvider.get());
    @SuppressWarnings("unchecked")
    Map<String, Object> variables = (Map<String, Object>) model;
    context.setVariables(variables);
    String retval = engine.process(view, context);
    return retval;
  }
}
