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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Singleton
public class MetricsCollector {
  final SortedMap<LocalDateTime, Map<String, Snapshot>> stored = new TreeMap<>();

  private final MetricRegistry metricRegistry;

  @Inject
  public MetricsCollector(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  public synchronized void sample() {
    LocalDateTime now = LocalDateTime.now();
    HashMap<String, Snapshot> map = new HashMap<>();
    for (Map.Entry<String, Timer> entry : metricRegistry.getTimers().entrySet()) {
      String key = entry.getKey();
      Snapshot snapshot = entry.getValue().getSnapshot();
      map.put(key, snapshot);
    }
    stored.put(now, map);
  }

  public SortedMap<LocalDateTime, Map<String, Snapshot>> getStored() {
    return stored;
  }
}
