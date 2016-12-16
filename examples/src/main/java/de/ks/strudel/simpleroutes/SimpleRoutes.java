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
package de.ks.strudel.simpleroutes;

import de.ks.strudel.Strudel;

public class SimpleRoutes {
  public static void main(final String[] args) {
    Strudel strudel = Strudel.create();
    strudel.get("/get", (request, response) -> "get");
    strudel.put("/put", (request, response) -> "put");
    strudel.post("/post", (request, response) -> "post");
    strudel.delete("/delete", (request, response) -> "delete");

    strudel.get("/wild/*", (request, response) -> "Wildcard route: " + request.routeWildcard());

    strudel.get("/user/{name}/page/{page}", (request, response) -> {
      String name = request.routeParameter("name");
      String page = request.routeParameter("page");
      return "Parameter route: user=" + name + ", page=" + page;
    });
    strudel.start();
  }
}
