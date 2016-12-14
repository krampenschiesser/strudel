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
package de.ks.strudel.mustache;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import de.ks.strudel.template.TemplateEngine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.StringWriter;

@Singleton
public class MustacheEngine implements TemplateEngine {
  private final MustacheFactory factory;

  @Inject
  public MustacheEngine(MustacheFactory factory) {
    this.factory = factory;
  }

  @Override
  public String render(Object model, String view) throws Exception {
    Mustache mustache = factory.compile(view);
    StringWriter writer = new StringWriter();
    mustache.execute(writer, model);
    String html = writer.toString();
    return html;
  }
}
