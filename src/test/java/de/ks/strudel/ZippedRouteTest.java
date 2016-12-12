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
import io.undertow.util.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(StrudelTestExtension.class)
public class ZippedRouteTest {
  @Inject
  Strudel strudel;

  @Test
  void zipRoute() {
    IntStream range = IntStream.range(0, 500000);
    String text = range.mapToObj(String::valueOf).collect(Collectors.joining());
    strudel.get("/zip", (request, response) -> text).gzip(true);
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/zip");
    assertEquals(text, response.body().asString());
    response.then().header(Headers.CONTENT_ENCODING.toString(), "gzip");
  }

  @Test
  void noZipForSmallData() {
    strudel.get("/zip", (request, response) -> "hallo").gzip(true);
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/zip");
    assertEquals("hallo", response.body().asString());
    String header = response.header(Headers.CONTENT_ENCODING_STRING);
    assertNull(header);
  }
}
