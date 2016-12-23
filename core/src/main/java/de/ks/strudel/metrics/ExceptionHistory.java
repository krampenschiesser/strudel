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
package de.ks.strudel.metrics;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionHistory {
  private final Map<String, StoredException> exceptionCache = new ConcurrentHashMap<>();
  private final int maxHistoryElements;

  public ExceptionHistory() {
    this(100);
  }

  public ExceptionHistory(int maxHistoryElements) {
    this.maxHistoryElements = maxHistoryElements;
  }

  public void trackException(Exception e) {
    StackTraceElement[] stackTrace = e.getStackTrace();

    StringBuilder b = new StringBuilder();
    b.append(e.getClass().getName()).append(":").append(e.getMessage()).append("\n");
    for (StackTraceElement stackTraceElement : stackTrace) {
      if (stackTraceElement.getClassName().startsWith("de.ks.strudel.route.handler.")) {
        break;
      }
      b.append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append(":").append(stackTraceElement.getLineNumber());
    }
    String key = b.toString();
    LocalDateTime now = LocalDateTime.now();
    StoredException storedException = exceptionCache.computeIfAbsent(key, k -> new StoredException(now, e, key));
    storedException.inc().setLastOccurance(now);

    if (exceptionCache.size() > maxHistoryElements) {
      StoredException oldest = exceptionCache.values().stream().min(Comparator.comparing(StoredException::getLastOccurance)).get();
      exceptionCache.remove(oldest.getKey());
    }
  }

  public Collection<StoredException> getExceptions() {
    return Collections.unmodifiableCollection(exceptionCache.values());
  }
}
