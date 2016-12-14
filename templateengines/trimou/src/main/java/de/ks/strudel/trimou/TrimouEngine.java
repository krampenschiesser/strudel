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

import de.ks.strudel.template.TemplateEngine;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;

import javax.inject.Inject;

public class TrimouEngine implements TemplateEngine {

  private final MustacheEngine engine;

  @Inject
  public TrimouEngine(MustacheEngine engine) {
    this.engine = engine;
  }

  @Override
  public String render(Object model, String view) throws Exception {
    Mustache mustache = engine.getMustache(view);
    String html = mustache.render(model);
    return html;
  }
}
