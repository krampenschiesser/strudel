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
package de.ks.strudel.simpledi;

import de.ks.strudel.Handler;
import de.ks.strudel.Request;
import de.ks.strudel.Response;

import javax.inject.Inject;
import java.util.Objects;

public class MyHandler implements Handler {
  private final Delegate delegate;

  @Inject
  public MyHandler(Delegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    StringBuilder b = new StringBuilder();
    b.append("Hi, i am instance ").append(Objects.hashCode(this)).append("<br/>\n");
    b.append("The delegate tells me you want locale ").append(delegate.getLocale()).append("<br/>\n");
    return b;
  }
}
