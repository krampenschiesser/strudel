package de.ks.strudel.pebble;

import de.ks.strudel.Strudel;
import de.ks.strudel.template.ModelAndView;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.hamcrest.Matchers.equalTo;

class PebbleEngineTest {
  private Strudel strudel;

  @BeforeEach
  void setUp() {
    strudel = Strudel.create(new PebbleModule());
    RestAssured.port = strudel.options().port();
    RestAssured.defaultParser = Parser.HTML;
  }

  @AfterEach
  void tearDown() {
    strudel.stop();
  }

  @Test
  void templateRendering() {
    strudel.get("/", (request, response) -> {
      HashMap<String, Object> model = new HashMap<>();
      model.put("title", "myTitle");
      model.put("condition", true);
      model.put("hello", "Hello Sauerland");
      return new ModelAndView(model, "index.html");
    }).template(PebbleEngine.class);

    strudel.start();

    Response response = RestAssured.get("/");
    System.out.println(response.body().asString());
    response.then().assertThat()//
            .body("html.head.title", equalTo("myTitle"))//
            .body("html.body.p", equalTo("Hello Sauerland"))//
            .body("html.body.b", equalTo("true"))//
            .body("html.body.h1", equalTo("Default")).body("html.body.div", equalTo("hello world"));

    response = RestAssured.get("/?lang=de");
    System.out.println(response.body().asString());
    response.then().assertThat()//
            .body("html.body.h1", equalTo("Deutsch"));
  }
}