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

import de.ks.strudel.route.HttpMethod;
import io.restassured.RestAssured;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static de.ks.strudel.util.StrudelTestExtension.wrap;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleRouteTest {
  private static final Logger log = LoggerFactory.getLogger(SimpleRouteTest.class);
  public static final String MSG_PREFIX = "your method is: ";

  @Inject
  Strudel strudel;

  @TestFactory
  Stream<DynamicTest> simplePathsAllMethods() {
    List<HttpMethod> httpMethods = Arrays.asList(HttpMethod.values());
    return httpMethods.stream().filter(m -> m != HttpMethod.ALL).map(m -> DynamicTest.dynamicTest("simple" + m.name(), wrap(this, () -> testHttpRoute(m))));
  }

  private void testHttpRoute(HttpMethod m) {
    String path = "/" + m.name();
    strudel.add(m, path, () -> (request, response) -> MSG_PREFIX + m.name());
    strudel.start();
    String body = RestAssured.request(m.name(), path).getBody().asString();
    assertEquals(MSG_PREFIX + m.name(), body);
  }

}
