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
package de.ks.strudel.route.handler.route;

import de.ks.strudel.route.Route;
import de.ks.strudel.route.handler.AsyncTracker;
import de.ks.strudel.route.handler.WrappingHandler;
import io.undertow.server.HttpServerExchange;

import javax.inject.Inject;

/**
 * Dispatches a itself to the worker thread
 */
public class ExecuteAsAsyncHandler extends WrappingHandler {
  protected final AsyncTracker asyncRoute;

  protected Route route;

  @Inject
  public ExecuteAsAsyncHandler(AsyncTracker asyncRoute) {
    this.asyncRoute = asyncRoute;
  }

  public ExecuteAsAsyncHandler setRoute(Route route) {
    this.route = route;
    return this;
  }

  @Override
  protected boolean before(HttpServerExchange ex) {
    if (route.isAsync() && ex.isInIoThread()) {
      asyncRoute.setAsyncRoute(true);
      ex.dispatch(this);
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected void after(HttpServerExchange exchange) {

  }
}
