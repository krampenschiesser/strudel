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

import de.ks.strudel.route.HttpStatus;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(StrudelTestExtension.class)
public class FilterTest {
  public static final String RESPONSE = "Should be visible!";
  @Inject
  Strudel strudel;

  @Test
  void filterBefore() {
    strudel.before("/secure/*", (request, response) -> {
      response.halt(HttpStatus.FORBIDDEN);
      return null;
    });
    strudel.get("/secure/bla", (request, response) -> "Should not be visible!");
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/secure/bla");
    response.then().statusCode(403);
    String body = response.body().asString();
    assertThat(body).isEmpty();
  }

  @Test
  void filterAfter() {
    AtomicBoolean called = new AtomicBoolean();

    strudel.after((request, response) -> {
      called.set(true);
      return null;
    });
    strudel.get("/secure/bla", (request, response) -> RESPONSE);
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/secure/bla");
    String body = response.body().asString();
    assertEquals(RESPONSE, body);

    assertTrue(called.get());
  }
}
