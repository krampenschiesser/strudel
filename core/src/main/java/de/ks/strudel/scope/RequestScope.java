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

import com.google.inject.*;
import de.ks.strudel.Response;
import de.ks.strudel.request.Request;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Basic scope starting before execution of a request and afer.
 * Not available during exception handling
 */
public class RequestScope implements Scope {
  private final ThreadLocal<Map<Key<?>, Object>> values = new ThreadLocal<>();

  public void enter(Request request, Response response, Locale locale) {
    HashMap<Key<?>, Object> map = new HashMap<>();
    map.put(Key.get(Request.class), request);
    map.put(Key.get(Response.class), response);
    map.put(Key.get(Locale.class), locale);
    values.set(map);
  }

  public void exit() {
    values.remove();
  }

  @Override public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    return () -> {
      Map<Key<?>, Object> scopedObjects = getScopedObjectMap(key);

      @SuppressWarnings("unchecked")
      T current = (T) scopedObjects.get(key);
      if (current == null && !scopedObjects.containsKey(key)) {
        current = unscoped.get();

        // don't remember proxies; these exist only to serve circular dependencies
        if (Scopes.isCircularProxy(current)) {
          return current;
        }

        scopedObjects.put(key, current);
      }
      return current;
    };
  }

  private <T> Map<Key<?>, Object> getScopedObjectMap(Key<T> key) {
    Map<Key<?>, Object> scopedObjects = values.get();
    if (scopedObjects == null) {
      throw new OutOfScopeException("Cannot access " + key + " outside of a scoping block");
    }
    return scopedObjects;
  }
}
