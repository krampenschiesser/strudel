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
import de.ks.strudel.route.FilterType;
import de.ks.strudel.route.HttpMethod;
import de.ks.strudel.route.RouteBuilder;
import de.ks.strudel.route.Router;
import de.ks.strudel.server.ServerManager;

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
    return add(HttpMethod.ANY, path, () -> handler).filter(FilterType.BEFORE);
  }

  public RouteBuilder after(String path, Handler handler) {
    return add(HttpMethod.ANY, path, () -> handler).filter(FilterType.AFTER);
  }

  public RouteBuilder before(Class<? extends Handler> handler) {
    return before("/*", handler);
  }

  public RouteBuilder after(Class<? extends Handler> handler) {
    return after("/*", handler);
  }

  public RouteBuilder before(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.ANY, path, () -> injector.getInstance(handler)).filter(FilterType.BEFORE);
  }

  public RouteBuilder after(String path, Class<? extends Handler> handler) {
    return add(HttpMethod.ANY, path, () -> injector.getInstance(handler)).filter(FilterType.AFTER);
  }

  public void exception(Class<? extends Exception> clazz, Class<? extends HandlerNoReturn> handler) {
    router.addExceptionHandler(clazz, handler);
  }

  public void exception(Class<? extends Exception> clazz, HandlerNoReturn handler) {
    router.addExceptionHandler(clazz, handler);
  }

  public StaticFiles classpathLocation(String classPathLocation, String url) {
    return addStaticFiles(classPathLocation, url, classPathFileHandlerProvider.get());
  }

  public StaticFiles externalLocation(String externalFolder, String url) {
    return addStaticFiles(externalFolder, url, folderFileHandlerProvider.get());
  }

  private StaticFiles addStaticFiles(String path, String url, StaticFileHandler handler) {
    StaticFiles staticFiles = new StaticFiles(path, url);
    handler.setStaticFileConfig(staticFiles);
    url = enhanceUrl(url);
    RouteBuilder builder = add(HttpMethod.ANY, url, () -> handler);
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
