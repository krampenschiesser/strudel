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

import com.google.common.base.StandardSystemProperty;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(StrudelTestExtension.class)
public class StaticFileTest {
  @Inject
  Strudel strudel;
  private static Path child;

  @BeforeAll
  static void beforeall() throws IOException {
    Path root = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value(), "staticfiles");
    if (Files.exists(root)) {
      new DeleteDir(root).delete();
    }
    Files.createDirectory(root);
    child = root.resolve("child");
    Files.createDirectory(child);
    Path childtxt = child.resolve("child.txt");
    Path roottxt = root.resolve("root.txt");
    Files.write(childtxt, Collections.singletonList("hello child!"));
    Files.write(roottxt, Collections.singletonList("hello root!"));
  }

  @BeforeEach
  void setUp() {
    strudel.classpathLocation("/de/ks", "/classpath");
    strudel.classpathLocation("/WEB-INF/public", "/webinf");
    strudel.classpathLocation("/de/ks/sub", "/sub");
    strudel.externalLocation(child.toString(), "/folder");
    strudel.start();
  }

  @Test
  void classPathServing() {
    String msg = RestAssured.get("/classpath/hello.txt").body().asString();
    assertEquals("hello sauerland!", msg);

    msg = RestAssured.get("/classpath/sub/sub.txt").body().asString();
    assertEquals("hello sub!", msg);

    msg = RestAssured.get("/sub/sub.txt").body().asString();
    assertEquals("hello sub!", msg);

    msg = RestAssured.get("/webinf/other.txt").body().asString();
    assertEquals("hello other!", msg);
  }

  @Test
  void pathEscaping() {
    RestAssured.get("/sub/sub.txt").then().statusCode(200);
    RestAssured.get("/sub/../hello.txt").then().statusCode(404);
    RestAssured.get("/sub/..\\hello.txt").then().statusCode(404);

    RestAssured.get("/folder/../main.txt").then().statusCode(404);
    RestAssured.get("/folder/..\\main.txt").then().statusCode(404);
    RestAssured.get("/folder/~/.bashrc").then().statusCode(404);
    RestAssured.get("/folder//.bashrc").then().statusCode(404);
  }

  @Test
  void filesFromFolder() {
    String msg = RestAssured.get("/classpath/hello.txt").body().asString();
    assertEquals("hello sauerland!", msg);

  }
}
