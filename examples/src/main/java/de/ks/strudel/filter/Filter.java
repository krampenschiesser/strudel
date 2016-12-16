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
package de.ks.strudel.filter;

import de.ks.strudel.HandlerNoReturn;
import de.ks.strudel.Strudel;
import de.ks.strudel.request.Request;
import de.ks.strudel.route.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Filter {
  private static final Logger log = LoggerFactory.getLogger(Filter.class);

  public static void main(final String[] args) {
    Strudel strudel = Strudel.create();
    strudel.before("/secure/*", (request, response) -> {
      if (!checkAuth(request)) {
        response.halt(HttpStatus.FORBIDDEN);
      }
    });
    strudel.get("/", (request, response) -> "i am the home");
    strudel.get("/secure/panel", (request, response) -> "Secure region");

    HandlerNoReturn before = (request, response) -> log.info("Before async execution");
    HandlerNoReturn after = (request, response) -> log.info("After async execution");
    strudel.get("/async", (request, response) -> "i am async").async(before, after);

    strudel.start();
  }

  private static boolean checkAuth(Request request) {
    return false;
  }
}
