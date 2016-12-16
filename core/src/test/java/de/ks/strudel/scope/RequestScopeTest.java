package de.ks.strudel.scope;

import de.ks.strudel.Handler;
import de.ks.strudel.Response;
import de.ks.strudel.Strudel;
import de.ks.strudel.request.Request;
import de.ks.strudel.util.StrudelTestExtension;
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
    strudel.get("/async", ScopedHandler.class).async();
    strudel.start();

    RestAssured.get("/bla").then().assertThat()//
               .statusCode(200)//
               .body(equalTo("/bla"));

    RestAssured.get("/async").then().assertThat()//
               .statusCode(200)//
               .body(equalTo("/async"));
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