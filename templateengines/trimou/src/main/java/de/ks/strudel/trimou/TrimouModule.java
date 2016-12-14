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
package de.ks.strudel.trimou;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import de.ks.strudel.template.TemplateEngine;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.locator.ClassPathTemplateLocator;
import org.trimou.handlebars.i18n.ResourceBundleHelper;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.Locale;

public class TrimouModule extends AbstractModule {
  private final String classpathPrefix;
  private final String resourceBundle;

  public TrimouModule() {
    this(null);
  }

  public TrimouModule(@Nullable String resourceBundle) {
    this(TemplateEngine.classPathPrefixNoSlash, resourceBundle);
  }

  public TrimouModule(String classpathPrefix, @Nullable String resourceBundle) {
    this.classpathPrefix = classpathPrefix;
    this.resourceBundle = resourceBundle;
  }

  @Override
  protected void configure() {

  }

  @Provides
  public MustacheEngine getTrimou(Provider<Locale> localeProvider) {
    MustacheEngineBuilder builder = MustacheEngineBuilder.newBuilder();
    builder.addTemplateLocator(new ClassPathTemplateLocator(1, classpathPrefix));
    if (resourceBundle != null) {
      ResourceBundleHelper resourceBundleHelper = new ResourceBundleHelper(resourceBundle);
      builder.registerHelper("i18n", resourceBundleHelper);
    }
    builder.setLocaleSupport(localeProvider::get);
    return builder.build();
  }
}
