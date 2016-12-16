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
package de.ks.strudel.gson;

import com.google.gson.Gson;
import de.ks.strudel.json.JsonParser;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GsonParser implements JsonParser {
  private final Gson gson;

  @Inject
  public GsonParser(Gson gson) {
    this.gson = gson;
  }

  @Override
  public String toString(Object object) {
    return gson.toJson(object);
  }

  @Override
  public <T> T fromString(String input, Class<T> clazz) throws Exception {
    return gson.fromJson(input, clazz);
  }
}
