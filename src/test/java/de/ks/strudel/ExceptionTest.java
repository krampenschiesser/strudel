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

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(StrudelTestExtension.class)
public class ExceptionTest {
  @Inject
  Strudel strudel;

  @Test
  void exceptionThrowing() {
    AtomicBoolean called = new AtomicBoolean();
    strudel.put("/exception", (request, response) -> {
      throw new IllegalArgumentException("nene");
    });
    strudel.exception(IllegalArgumentException.class, (request, response) -> {
      called.set(true);
    });
    strudel.start();

    RestAssured.put("/exception").then().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.getValue());
    assertTrue(called.get());
  }

  @Test
  void childException() {
    AtomicBoolean called = new AtomicBoolean();
    strudel.put("/exception", (request, response) -> {
      throw new IllegalThreadStateException("nene");
    });
    strudel.exception(IllegalArgumentException.class, (request, response) -> {
      called.set(true);
    });
    strudel.start();

    RestAssured.put("/exception").then().statusCode(HttpStatus.INTERNAL_SERVER_ERROR.getValue());
    assertTrue(called.get());
  }
}
