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
package de.ks.strudel.json;

import com.google.common.net.MediaType;
import com.google.gson.Gson;
import de.ks.strudel.Strudel;
import de.ks.strudel.util.StrudelTestExtension;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.*;

import static org.hamcrest.Matchers.equalTo;

@ExtendWith(StrudelTestExtension.class)
public class JSonParserTest {
  @Inject
  Strudel strudel;

  @Test
  void returnJson() {
    strudel.get("/", (request, response) -> new MyPojo()).json(MyJsonParser.class);
    strudel.start();

    RestAssured.defaultParser = Parser.JSON;
    Response response = RestAssured.get("/");
    System.out.println(response.body().asString());
    response.then().contentType(MediaType.JSON_UTF_8.type());
    response.then()//
            .body("list", equalTo(Arrays.asList("Hello", "World")))//
            .body("data.hello", equalTo("World"));
  }

  static class MyPojo {
    Map<Object, Object> data = new HashMap<>();
    List<String> list = new ArrayList<>();

    public MyPojo() {
      data.put("hello", "World");
      list.add("Hello");
      list.add("World");
    }
  }

  static class MyJsonParser implements JsonParser {
    @Override
    public String toString(Object object) {
      return new Gson().toJson(object);
    }

    @Override
    public <T> T fromString(String input, Class<T> clazz) throws Exception {
      return null;
    }
  }
}
