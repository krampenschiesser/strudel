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
package de.ks.strudel.jade;

import de.ks.strudel.template.TemplateEngine;
import de.neuland.jade4j.JadeConfiguration;
import de.neuland.jade4j.template.JadeTemplate;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class JadeEngine implements TemplateEngine {
  private final JadeConfiguration config;

  @Inject
  public JadeEngine(JadeConfiguration config) {
    this.config = config;
  }

  @Override
  public String render(Object model, String view) throws Exception {
    JadeTemplate template = config.getTemplate(view);
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) model;
    String html = config.renderTemplate(template, data);
    return html;
  }

}
