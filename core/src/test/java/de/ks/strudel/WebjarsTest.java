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
import io.undertow.util.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

@ExtendWith(StrudelTestExtension.class)
public class WebjarsTest {
  @Inject
  Strudel strudel;

  @Test
  void webjars() {
    strudel.webjars();
    strudel.start();

    io.restassured.response.Response response = RestAssured.get("/webjars/jquery/2.2.1/jquery.min.js");
    response.then().statusCode(200);
    response.then().header(Headers.CONTENT_ENCODING_STRING, "gzip");
  }
}
