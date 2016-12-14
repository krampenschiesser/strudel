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
package de.ks.strudel.pebble;

import com.mitchellbosecke.pebble.template.PebbleTemplate;
import de.ks.strudel.template.TemplateEngine;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

@Singleton
public class PebbleEngine implements TemplateEngine {

  private final com.mitchellbosecke.pebble.PebbleEngine engine;
  private final Provider<Locale> localeProvider;

  @Inject
  public PebbleEngine(Provider<Locale> localeProvider, com.mitchellbosecke.pebble.PebbleEngine engine) {
    this.localeProvider = localeProvider;
    this.engine = engine;
  }

  @Override
  public String render(Object model, String view) throws Exception {
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) model;
    PebbleTemplate compiledTemplate = engine.getTemplate(view);
    StringWriter writer = new StringWriter();
    compiledTemplate.evaluate(writer, data, localeProvider.get());
    return writer.toString();
  }
}
