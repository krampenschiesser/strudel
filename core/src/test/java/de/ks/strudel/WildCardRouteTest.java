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

import io.restassured.RestAssured;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(StrudelTestExtension.class)
public class WildCardRouteTest {
  @Inject
  Strudel strudel;

  @Test
  void paramaterGet() {
    strudel.get("/say/{first}/to/{second}", (request, response) -> {
      String first = request.routeParameter("first");
      String second = request.routeParameter("second");
      return first + " to " + second;
    });
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/say/hello/to/me");
    response.then().statusCode(200);
    String body = response.body().asString();
    assertEquals("hello to me", body);

  }

  @Test
  @Disabled
  void paramaterGetLongEnd() {
    strudel.get("/say/{first}/to/{second}", (request, response) -> {
      String first = request.routeParameter("first");
      String second = request.routeParameter("second");
      return first + " to " + second;
    });
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/say/hello/to/me/you");
    response.then().statusCode(200);
    String body = response.body().asString();
    assertEquals("hello to me", body);
  }

  @Test
  void wildCardGet() {
    strudel.get("/secure*", (request, response) -> {
      return "is secure: " + request.routeWildcard();
    });
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/secure/admin/panel");
    assertEquals(200, response.statusCode(), "Could not get /secure/admin/panel");
    String body = response.body().asString();
    assertEquals("is secure: /admin/panel", body);

    response = RestAssured.get("/secure/panel");
    assertEquals(200, response.statusCode(), "Could not get /secure/panel");
    body = response.body().asString();
    assertEquals("is secure: /panel", body);

    RestAssured.get("/bla").then().statusCode(404);
    RestAssured.get("/bla/blubb").then().statusCode(404);
    RestAssured.get("/bla/panel").then().statusCode(404);
  }
}
