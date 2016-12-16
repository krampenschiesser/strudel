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
package de.ks.strudel.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ks.strudel.json.JsonParser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class JacksonParser implements JsonParser {
  private final ObjectMapper objectMapper;

  @Inject
  public JacksonParser(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String parse(Object object) throws Exception {
    return objectMapper.writeValueAsString(object);
  }
}
