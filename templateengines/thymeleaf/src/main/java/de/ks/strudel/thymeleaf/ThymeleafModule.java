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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

public class ThymeleafModule extends AbstractModule {
  protected final String prefix;

  public ThymeleafModule() {
    this(de.ks.strudel.template.TemplateEngine.classPathPrefixNoSlash);
  }

  public ThymeleafModule(String prefix) {
    this.prefix = prefix;
  }

  @Override
  protected void configure() {
  }

  @Provides
  public ITemplateResolver getTemplateResolver() {
    ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
    templateResolver.setSuffix(".html");
    templateResolver.setPrefix(prefix);
    return templateResolver;
  }

  @Provides
  public TemplateEngine getEngine(ITemplateResolver resolver) {
    TemplateEngine engine = new TemplateEngine();
    engine.setTemplateResolver(resolver);
    return engine;
  }
}
