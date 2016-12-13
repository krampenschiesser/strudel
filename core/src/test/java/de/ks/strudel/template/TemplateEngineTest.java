package de.ks.strudel.template;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.strudel.Strudel;
import de.ks.strudel.StrudelModule;
import de.ks.strudel.StrudelTestExtension;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateEngineTest {
  Strudel strudel;

  @BeforeEach
  void setUp() {
    Injector injector = Guice.createInjector(new StrudelModule(), new TestModule());
    strudel = injector.getInstance(Strudel.class);
    strudel.options().port(StrudelTestExtension.port);
    RestAssured.port = StrudelTestExtension.port;
    RestAssured.defaultParser = Parser.HTML;
  }

  @AfterEach
  void tearDown() {
    strudel.stop();
  }

  @Test
  void templateRoute() {
    strudel.get("/template", (request, response) -> {
      HashMap<String, String> model = new HashMap<>();
      model.put("hello", "sauerland");
      String view = "myview.template";
      return new ModelAndView(model, view);
    }).template();

    strudel.get("/other", (request, response) -> new ModelAndView(null, "")).template(OtherEngine.class);
    strudel.start();

    String body = RestAssured.get("/template").body().asString();
    assertEquals("Hello sauerland you are displayed in myview.template", body);

    body = RestAssured.get("/other").body().asString();
    assertEquals("other", body);
  }

  @Test
  void noModelAndView() {
    strudel.get("/template", (request, response) -> "bla").template();
    strudel.start();

    RestAssured.get("/template").then().statusCode(500);
  }

  public static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TemplateEngine.class).to(TestTemplateEngine.class);
    }
  }

  static class OtherEngine implements TemplateEngine {

    @Override
    public String render(Object model, String view) {
      return "other";
    }
  }

  static class TestTemplateEngine implements TemplateEngine {
    @Override
    public String render(Object model, String view) {
      @SuppressWarnings("unchecked")
      Map<String, String> data = (Map<String, String>) model;
      return "Hello " + data.get("hello") + " you are displayed in " + view;
    }
  }
}