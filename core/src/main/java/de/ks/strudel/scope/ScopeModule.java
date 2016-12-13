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
package de.ks.strudel.scope;

import com.google.inject.AbstractModule;
import de.ks.strudel.Request;
import de.ks.strudel.Response;

public class ScopeModule extends AbstractModule {
  @Override
  public void configure() {
    RequestScope requestScope = new RequestScope();

    bindScope(RequestScoped.class, requestScope);
    bind(RequestScope.class).toInstance(requestScope);
    bind(Request.class).in(requestScope);
    bind(Response.class).in(requestScope);
//    bind(HttpServerExchange.class).in(requestScope);
  }
}
