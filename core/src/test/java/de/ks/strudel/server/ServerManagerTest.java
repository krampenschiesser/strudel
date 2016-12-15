package de.ks.strudel.server;

import de.ks.strudel.localization.LocaleResolver;
import de.ks.strudel.option.Options;
import de.ks.strudel.route.RouteBuilder;
import de.ks.strudel.route.Router;
import de.ks.strudel.scope.RequestScope;
import io.restassured.RestAssured;
import io.undertow.Undertow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerManagerTest {

  public static final String GREETING = "Hello World";
  private ServerManager serverManager;

  @BeforeEach
  public void startServer() {
    Options options = new Options().port(0);//any
    RequestScope scope = Mockito.mock(RequestScope.class);
    LocaleResolver localeResolver = Mockito.mock(LocaleResolver.class);
    Router router = new Router(null, scope, null, localeResolver, null);
    router.addRoute(new RouteBuilder().get().path("/hello").handler(() -> (request, response) -> GREETING).build());
    serverManager = new ServerManager(options, router);
    serverManager.start();
    RestAssured.port = getRunningPort(serverManager.getUndertow());
  }

  int getRunningPort(Undertow undertow) {
    Undertow.ListenerInfo listenerInfo = undertow.getListenerInfo().get(0);
    SocketAddress address = listenerInfo.getAddress();
    InetSocketAddress cast = (InetSocketAddress) address;
    return cast.getPort();
  }

  @AfterEach
  public void stopServer() {
    serverManager.stop();
  }

  @Test
  public void testIntegration() {
    RestAssured.get("/hello").then().statusCode(200);
    assertEquals(GREETING, RestAssured.get("/hello").asString());
  }
}