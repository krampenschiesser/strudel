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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;

public class FreemarkerModule extends AbstractModule {
  private final String classpathPrefix;

  public FreemarkerModule() {
    this(de.ks.strudel.template.TemplateEngine.classPathPrefix);
  }

  public FreemarkerModule(String classpathPrefix) {
    this.classpathPrefix = classpathPrefix;
  }


  @Override
  protected void configure() {

  }

  @Provides
  public Configuration getConfiguration() {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_25);
    configuration.setTemplateLoader(new ClassTemplateLoader(getClass(), classpathPrefix));
    return configuration;
  }
}
