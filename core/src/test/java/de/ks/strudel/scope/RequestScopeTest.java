package de.ks.strudel.scope;

import de.ks.strudel.*;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(StrudelTestExtension.class)
class RequestScopeTest {
  @Inject
  Strudel strudel;

  @Test
  void scoping() {
    strudel.get("/bla", ScopedHandler.class);
    strudel.start();

    RestAssured.get("/bla").then().assertThat()//
               .statusCode(200)//
               .body(equalTo("/bla"));
  }

  public static class ScopedHandler implements Handler {
    @Inject
    Request req;

    @Override
    public Object handle(Request request, Response response) throws Exception {
      assertNotNull(req);
      return req.path();
    }
  }
}