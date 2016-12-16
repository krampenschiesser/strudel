package de.ks.strudel.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import com.google.inject.AbstractModule;
import de.ks.strudel.Strudel;
import de.ks.strudel.json.JsonParser;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

class JacksonParserTest {
  private Strudel strudel;

  @BeforeEach
  void setUp() {
    strudel = Strudel.create(new TestModule());
    RestAssured.port = strudel.options().port();
    RestAssured.defaultParser = Parser.JSON;
  }

  @AfterEach
  void tearDown() {
    strudel.stop();
  }

  @Test
  void parsing() {
    strudel.get("/", (request, response) -> new MyPojo("me", 30)).json();
    strudel.start();

    RestAssured.get("/").then()//
               .contentType(MediaType.JSON_UTF_8.type())//
               .body("name", equalTo("me"))//
               .body("age", equalTo(30));
  }

  @Test
  void receiveParsing() throws JsonProcessingException {
    strudel.post("/", (request, response) -> {
      MyPojo myPojo = request.bodyFromJson(MyPojo.class);
      return myPojo;
    }).json();

    strudel.start();

    String json = new ObjectMapper().writeValueAsString(new MyPojo("hello", 41));
    RestAssured.given().body(json)//
               .post("/").then()//
               .statusCode(200)//
               .contentType(MediaType.JSON_UTF_8.type())//
               .body("name", equalTo("hello"))//
               .body("age", equalTo(41));
  }

  static class TestModule extends AbstractModule {
    @Override
    protected void configure() {
      install(new JacksonModule());
      bind(JsonParser.class).to(JacksonParser.class);
    }
  }

  static class MyPojo {
    String name;
    int age;

    protected MyPojo() {
      //jackson
    }

    public MyPojo(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public int getAge() {
      return age;
    }

    public String getName() {
      return name;
    }
  }
}