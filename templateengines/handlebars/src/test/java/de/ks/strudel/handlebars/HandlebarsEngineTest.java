package de.ks.strudel.handlebars;

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

class HandlebarsEngineTest {
  private Strudel strudel;

  @BeforeEach
  void setUp() {
    strudel = Strudel.create(new HandlebarsModule());
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
      return new ModelAndView(model, "index");
    }).template(HandlebarsEngine.class);

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