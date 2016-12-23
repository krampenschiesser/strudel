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
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class StoredException {
  final AtomicReference<LocalDateTime> lastOccurance = new AtomicReference<>(LocalDateTime.now());
  final LocalDateTime firstOccurance;
  final Exception exception;
  final AtomicLong count = new AtomicLong(0);
  final String key;

  public StoredException(LocalDateTime dateTime, Exception exception, String key) {
    this.key = key;
    lastOccurance.set(dateTime);
    firstOccurance = dateTime;
    this.exception = exception;
  }

  public String getKey() {
    return key;
  }

  public LocalDateTime getFirstOccurance() {
    return firstOccurance;
  }

  public LocalDateTime getLastOccurance() {
    return lastOccurance.get();
  }

  public StoredException setLastOccurance(LocalDateTime dateTime) {
    this.lastOccurance.set(dateTime);
    return this;
  }

  public Exception getException() {
    return exception;
  }

  public long getCount() {
    return count.get();
  }

  public StoredException inc() {
    count.incrementAndGet();
    return this;
  }
}
