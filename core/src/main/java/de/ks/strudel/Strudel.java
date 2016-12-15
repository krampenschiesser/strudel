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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import de.ks.strudel.handler.ClassPathFileHandler;
import de.ks.strudel.handler.FolderFileHandler;
import de.ks.strudel.handler.StaticFileHandler;
import de.ks.strudel.option.Options;
import de.ks.strudel.route.*;
import de.ks.strudel.server.ServerManager;
import de.ks.strudel.websocket.OnWebSocketOpen;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Singleton
public class Strudel {
  public static Strudel create(Module... modules) {
    ArrayList<Module> all = new ArrayList<>();
    all.addAll(Arrays.asList(modules));
    all.add(new StrudelModule());
    Injector injector = Guice.createInjector(all);
    return injector.getInstance(Strudel.class);
  }

  final Options options;
  final Router router;
  private final ServerManager serverManager;
  private final Provider<ClassPathFileHandler> classPathFileHandlerProvider;
  private final Provider<FolderFileHandler> folderFileHandlerProvider;
  private final List<RouteBuilder> builders = new ArrayList<>();
  private final Injector injector;

  @Inject
  public Strudel(Injector injector, Options options, Router router, ServerManager serverManager, Provider<ClassPathFileHandler> classPathFileHandlerProvider, Provider<FolderFileHandler> folderFileHandlerProvider) {
    this.injector = injector;
    this.options = options;
    this.router = router;
    this.serverManager = serverManager;
    this.classPathFileHandlerProvider = classPathFileHandlerProvider;
    this.folderFileHandlerProvider = folderFileHandlerProvider;
  }

  public Options options() {
    return options;
  }

  public RouteBuilder all(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.ALL, path, () -> injector.getInstance(handler));
  }

  public RouteBuilder all(String path, Handler handler) {
    return add(HttpMethod.ALL, path, () -> handler);
  }

  public RouteBuilder get(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.GET, path, () -> injector.getInstance(handler));
  }

  public RouteBuilder get(String path, Handler handler) {
    return add(HttpMethod.GET, path, () -> handler);
  }

  public RouteBuilder put(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.PUT, path, () -> injector.getInstance(handler));
  }

  public RouteBuilder put(String path, Handler handler) {
    return add(HttpMethod.PUT, path, () -> handler);
  }

  public RouteBuilder post(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.POST, path, () -> injector.getInstance(handler));
  }

  public RouteBuilder post(String path, Handler handler) {
    return add(HttpMethod.POST, path, () -> handler);
  }

  public RouteBuilder delete(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.DELETE, path, () -> injector.getInstance(handler));
  }

  public RouteBuilder delete(String path, Handler handler) {
    return add(HttpMethod.DELETE, path, () -> handler);
  }

  public RouteBuilder before(Handler handler) {
    return before("/*", handler);
  }

  public RouteBuilder after(Handler handler) {
    return after("/*", handler);
  }

  public RouteBuilder before(String path, Handler handler) {
    return add(HttpMethod.ALL, path, () -> handler).filter(FilterType.BEFORE);
  }

  public RouteBuilder after(String path, Handler handler) {
    return add(HttpMethod.ALL, path, () -> handler).filter(FilterType.AFTER);
  }

  public RouteBuilder before(Class<? extends Handler> handler) {
    return before("/*", handler);
  }

  public RouteBuilder after(Class<? extends Handler> handler) {
    return after("/*", handler);
  }

  public RouteBuilder before(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.ALL, path, () -> injector.getInstance(handler)).filter(FilterType.BEFORE);
  }

  public RouteBuilder after(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.ALL, path, () -> injector.getInstance(handler)).filter(FilterType.AFTER);
  }

  public void exception(Class<? extends Exception> clazz, Class<? extends HandlerNoReturn> handler) {
    router.addExceptionHandler(clazz, handler);
  }

  public void exception(Class<? extends Exception> clazz, HandlerNoReturn handler) {
    router.addExceptionHandler(clazz, handler);
  }

  public StaticFiles classpathLocation(String classPathLocation, String path) {
    return addStaticFiles(classPathLocation, path, classPathFileHandlerProvider.get());
  }

  public StaticFiles externalLocation(String externalFolder, String path) {
    return addStaticFiles(externalFolder, path, folderFileHandlerProvider.get());
  }

  public RouteBuilder webjars() {
    return webjars("/webjars");
  }

  public RouteBuilder webjars(String path) {
    return classpathLocation("META-INF/resources/webjars/", path).getRouteBuilder().gzip();
  }

  public void websocket(String path, @Nullable OnWebSocketOpen openCallback, Supplier<AbstractReceiveListener> listener) {
    WebSocketConnectionCallback callback = new WebSocketConnectionCallback() {
      @Override
      public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        if (openCallback != null) {
          openCallback.accept(exchange, channel);
        }
        channel.getReceiveSetter().set(listener.get());
        channel.resumeReceives();
      }
    };
    websocket(path, callback);
  }

  public void websocket(String path, WebSocketConnectionCallback webSocketConnectionCallback) {
    checkStopped();
    RouteBuilder builder = new RouteBuilder().method(HttpMethod.GET).path(path);
    new BuilderFriend(builder).websocket(webSocketConnectionCallback);
    builders.add(builder);
  }

  private StaticFiles addStaticFiles(String path, String url, StaticFileHandler handler) {
    StaticFiles staticFiles = new StaticFiles(path, url);
    handler.setStaticFileConfig(staticFiles);
    url = enhanceUrl(url);
    RouteBuilder builder = add(HttpMethod.ALL, url, () -> handler);
    return staticFiles.setRouteBuilder(builder);
  }

  private String enhanceUrl(String url) {
    if (!url.endsWith("*/")) {
      if (url.endsWith("/")) {
        url += "*";
      } else {
        url += "/*";
      }
    }
    return url;
  }

  public RouteBuilder add(HttpMethod method, String path, Supplier<Handler> handler) {
    checkStopped();
    RouteBuilder routeBuilder = new RouteBuilder().method(method).path(path).handler(handler);
    if (method == HttpMethod.POST || method == HttpMethod.PUT) {
      routeBuilder.async();//because of input stream parsing in undertow
    }
    builders.add(routeBuilder);
    return routeBuilder;
  }

  public void start() {
    builders.stream().map(RouteBuilder::build).forEach(router::addRoute);//flush routes to undertow
    builders.clear();
    serverManager.start();
  }

  public void stop() {
    serverManager.stop();
  }

  public ServerManager getServerManager() {
    return serverManager;
  }

  protected void checkStopped() {
    if (serverManager.isStarted()) {
      throw new IllegalStateException("Configuring routes is only allowed when stopped");
    }
  }

}
