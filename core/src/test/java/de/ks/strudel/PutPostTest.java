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

import de.ks.strudel.util.StrudelTestExtension;
import io.restassured.RestAssured;
import io.undertow.server.handlers.form.FormData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(StrudelTestExtension.class)
public class PutPostTest {
  @Inject
  Strudel strudel;

  @Test
  void put() {
    strudel.put("/put", (request, response) -> request.body());
    strudel.start();

    io.restassured.response.Response put = RestAssured.given().body("hello world").put("/put");
    put.then().statusCode(200);
    assertEquals("hello world", put.body().asString());
  }

  @Test
  void post() {
    strudel.post("/post", (request, response) -> request.body());
    strudel.start();

    io.restassured.response.Response post = RestAssured.given().body("hello world").post("/post");
    post.then().statusCode(200);
    assertEquals("hello world", post.body().asString());
  }

  @Test
  void longPost() {
    strudel.post("/post", (request, response) -> request.body());
    strudel.start();

    String body = IntStream.range(0, 1_0000_000).mapToObj(String::valueOf).collect(Collectors.joining("\n"));
    io.restassured.response.Response post = RestAssured.given().body(body).post("/post");
    post.then().statusCode(200);
    assertEquals(body, post.body().asString());
  }

  @Test
  void postFormdata() {
    strudel.post("/post", (request, response) -> request.formData().get("hello").peekFirst().getValue());
    strudel.start();

    io.restassured.response.Response post = RestAssured.given().formParam("hello", "sauerland").post("/post");
    post.then().statusCode(200);
    assertEquals("sauerland", post.body().asString());
  }

  @Test
  void fileUpload() {
    strudel.post("/post", (request, response) -> {
      FormData.FormValue value = request.formData().get("file").peekFirst();
      if (value.isFile()) {
        Path path = value.getPath();
        String content = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
        return content;
      }
      return null;
    });
    strudel.start();

    File file = new File(getClass().getResource("/WEB-INF/public/other.txt").getFile());
    io.restassured.response.Response post = RestAssured.given()//
                                                       .multiPart(file)//
                                                       .post("/post");
    post.then().statusCode(200);
    assertEquals("hello other!", post.body().asString());

  }
}