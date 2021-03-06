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
package de.ks.strudel.freemarker;

import de.ks.strudel.template.TemplateEngine;
import freemarker.template.Configuration;
import freemarker.template.Template;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.util.Locale;

@Singleton
public class FreemarkerEngine implements TemplateEngine {
  private final Configuration configuration;
  private final Provider<Locale> localeProvider;

  @Inject
  public FreemarkerEngine(Provider<Locale> localeProvider, Configuration configuration) {
    this.localeProvider = localeProvider;
    this.configuration = configuration;
  }

  @Override
  public String render(Object model, String view) throws Exception {
    Template template = configuration.getTemplate(view, localeProvider.get());
    StringWriter out = new StringWriter();
    template.process(model, out);
    return out.toString();
  }
}
