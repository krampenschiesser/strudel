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
package de.ks.strudel.handlebars;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import de.ks.strudel.template.TemplateEngine;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Locale;

public class HandlebarsEngine implements TemplateEngine {
  private final Provider<Locale> localeProvider;
  private final Handlebars handlebars;

  @Inject
  public HandlebarsEngine(Provider<Locale> localeProvider) {
    this.localeProvider = localeProvider;
    handlebars = new Handlebars(new ClassPathTemplateLoader(classPathPrefix));
  }

  @Override
  public String render(Object model, String view) throws Exception {
    Template template = handlebars.compile(view);
    Context context = Context.newBuilder(model).build().combine("locale", localeProvider.get().getLanguage());
    String html = template.apply(context);
    return html;
  }
}
