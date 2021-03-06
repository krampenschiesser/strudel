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

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.ks.strudel.template.TemplateEngine;

public class HandlebarsModule extends AbstractModule {
  private String classPathPrefix;

  public HandlebarsModule() {
    this(TemplateEngine.classPathPrefix);
  }

  public HandlebarsModule(String classPathPrefix) {
    this.classPathPrefix = classPathPrefix;
  }

  @Override
  protected void configure() {

  }

  @Provides
  public Handlebars getHandlebars() {
    return new Handlebars(new ClassPathTemplateLoader(classPathPrefix));
  }
}
