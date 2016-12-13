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
package de.ks.strudel;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
  public static void main(final String[] args) {
    Injector injector = Guice.createInjector(new StrudelModule());
    Strudel strudel = injector.getInstance(Strudel.class);

    strudel.get("/long", (req, resp) -> {
      String bla = "";
      for (int i = 0; i < 1000; i++) {
        bla += "Hello world\n";
      }
      return bla;
    }).gzip();
    strudel.get("/short", (req, resp) -> "Hello world\n").gzip();
    IntStream range = IntStream.range(0, 500000);
    String text = range.mapToObj(String::valueOf).collect(Collectors.joining());
    strudel.get("/zip", (request, response) -> text).gzip();
    strudel.get("/hello", (req, resp) -> "Hello world!");
    strudel.get("/", (req, resp) -> "Hello Sauerland!");

    strudel.start();
  }
}
