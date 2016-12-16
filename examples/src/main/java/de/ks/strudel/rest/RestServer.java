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
package de.ks.strudel.rest;

import com.google.inject.AbstractModule;
import de.ks.strudel.Strudel;
import de.ks.strudel.gson.GsonModule;
import de.ks.strudel.gson.GsonParser;
import de.ks.strudel.jackson.JacksonParser;
import de.ks.strudel.json.JsonParser;

public class RestServer {
  public static void main(final String[] args) {
    Strudel strudel = Strudel.create(new JsonModule());
    strudel.get("/", (request, response) -> new MyPojo("Hans Wurst GSon", 42)).json();
    strudel.get("/jackson", (request, response) -> new MyPojo("Hans Wurst Jackson", 42)).json(JacksonParser.class);
    strudel.start();
  }

  static class JsonModule extends AbstractModule {
    @Override
    protected void configure() {
      install(new GsonModule());
      bind(JsonParser.class).to(GsonParser.class);
    }
  }

  static class MyPojo {
    String name;
    int age;

    public MyPojo(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }
  }
}
