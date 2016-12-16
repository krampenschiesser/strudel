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
package de.ks.strudel.async;

import de.ks.strudel.Handler;
import de.ks.strudel.Strudel;

import java.util.concurrent.TimeUnit;

public class AsyncGet {
  public static void main(final String[] args) {
    Handler handler = (request, response) -> {
      Thread.sleep(TimeUnit.SECONDS.toMillis(10));
      return "hello sauerland!";
    };

    Strudel strudel = Strudel.create();
    strudel.get("/", handler).async();
    strudel.get("/fast", (request, response) -> "hello fast");
    strudel.start();
  }
}
