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
package de.ks.strudel.multipleinstance;

import de.ks.strudel.Strudel;

public class MultipleInstances {

  public static void main(final String[] args) {
    Strudel first = Strudel.create();
    Strudel second = Strudel.create();
    first.get("/", (request, response) -> "hello I am server 1");
    second.get("/", (request, response) -> "hello I am server 2");

    first.options().port(8000);
    second.options().port(8001);

    first.start();
    second.start();
  }
}
