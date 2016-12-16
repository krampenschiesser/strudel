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
package de.ks.strudel.guice.scope;

import de.ks.strudel.Handler;
import de.ks.strudel.Response;
import de.ks.strudel.request.Request;

import javax.inject.Inject;
import java.util.Locale;

public class MyBean implements Handler {
  @Inject
  Request request;
  @Inject
  Response response;
  @Inject
  Locale locale;

  @Override
  public Object handle(Request _request, Response _response) throws Exception {
    return "Your address is: " + request.sourceAddress() + " and your locale is " + locale.getDisplayName();
  }
}
