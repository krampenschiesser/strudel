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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import de.ks.strudel.template.TemplateEngine;

public class PebbleModule extends AbstractModule {
  private final String classPathPrefix;

  public PebbleModule() {
    this(TemplateEngine.classPathPrefixNoSlash);
  }

  public PebbleModule(String classPathPrefix) {
    this.classPathPrefix = classPathPrefix;
  }

  @Override
  protected void configure() {

  }

  @Provides
  public com.mitchellbosecke.pebble.PebbleEngine getEngine() {
    ClasspathLoader loader = new ClasspathLoader(getClass().getClassLoader());
    loader.setPrefix(classPathPrefix);

    PebbleEngine.Builder builder = new PebbleEngine.Builder();
    return builder.loader(loader)//
                  .build();
  }
}
