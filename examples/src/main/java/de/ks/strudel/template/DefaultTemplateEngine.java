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
package de.ks.strudel.template;

import com.google.inject.AbstractModule;
import de.ks.strudel.Strudel;
import de.ks.strudel.trimou.TrimouEngine;
import de.ks.strudel.trimou.TrimouModule;

import java.util.HashMap;
import java.util.Map;

/**
 * See: WEB-INF/template/trimouhello.html
 */
public class DefaultTemplateEngine {
  public static void main(final String[] args) {
    Strudel strudel = Strudel.create(new TemplateModule(), new TrimouModule());
    strudel.get("/", (request, response) -> {
      Map<String, String> model = new HashMap<>();
      model.put("title", "Hello Title!");
      model.put("hello", "Hello Sauerland!");
      return new ModelAndView(model, "trimouhello.html");
    }).template();
    strudel.start();
  }

  static class TemplateModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TemplateEngine.class).to(TrimouEngine.class);
    }
  }
}
