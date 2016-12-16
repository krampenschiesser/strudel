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

/**
 * Wrapper for single return value
 */
public class ModelAndView {
  private final Object model;
  private final String templateName;

  public ModelAndView(Object model, String templateName) {
    this.model = model;
    this.templateName = templateName;
  }

  public Object getModel() {
    return model;
  }

  public String getTemplateName() {
    return templateName;
  }
}
