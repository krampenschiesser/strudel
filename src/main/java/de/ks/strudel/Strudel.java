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

import de.ks.strudel.option.Options;
import de.ks.strudel.route.FilterType;
import de.ks.strudel.route.HttpMethod;
import de.ks.strudel.route.RouteBuilder;
import de.ks.strudel.route.Router;
import de.ks.strudel.server.ServerManager;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class Strudel {
  Options options;
  Router router;
  private final ServerManager serverManager;
  private final List<RouteBuilder> builders = new ArrayList<>();

  @Inject
  public Strudel(Options options, Router router, ServerManager serverManager) {
    this.options = options;
    this.router = router;
    this.serverManager = serverManager;
  }

  public Options options() {
    return options;
  }

  public void get(String path, Handler handler) {
    get(path, null, handler);
  }

  public void get(String path, @Nullable Consumer<RouteBuilder> enhancer, Handler handler) {
    add(HttpMethod.GET, path, enhancer, handler);
  }

  public void put(String path, Handler handler) {
    put(path, null, handler);
  }

  public void put(String path, @Nullable Consumer<RouteBuilder> enhancer, Handler handler) {
    add(HttpMethod.PUT, path, enhancer, handler);
  }

  public void post(String path, Handler handler) {
    post(path, null, handler);
  }

  public void post(String path, @Nullable Consumer<RouteBuilder> enhancer, Handler handler) {
    add(HttpMethod.POST, path, enhancer, handler);
  }

  public void delete(String path, Handler handler) {
    delete(path, null, handler);
  }

  public void delete(String path, @Nullable Consumer<RouteBuilder> enhancer, Handler handler) {
    add(HttpMethod.DELETE, path, enhancer, handler);
  }

  public RouteBuilder before(Handler handler) {
    return before("/*", handler);
  }

  public RouteBuilder after(Handler handler) {
    return after("/*", handler);
  }

  public RouteBuilder before(String path, Handler handler) {
    return add(HttpMethod.ANY, path, null, handler).filter(FilterType.BEFORE);
  }

  public RouteBuilder after(String path, Handler handler) {
    return add(HttpMethod.ANY, path, null, handler).filter(FilterType.AFTER);
  }

  public void exception(Class<? extends Exception> clazz, HandlerNoReturn handler) {
    router.addExceptionHandler(clazz, handler);
  }

  public RouteBuilder add(HttpMethod method, String path, @Nullable Consumer<RouteBuilder> enhancer, Handler handler) {
    checkStopped();
    RouteBuilder routeBuilder = new RouteBuilder().method(method).path(path).handler(handler).async();
    if (enhancer != null) {
      enhancer.accept(routeBuilder);
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
