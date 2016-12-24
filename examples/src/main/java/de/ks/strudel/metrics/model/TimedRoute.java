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
package de.ks.strudel.metrics.model;

import com.codahale.metrics.Snapshot;

import java.time.LocalDateTime;
import java.util.*;

public class TimedRoute {
  List<Dataset> datasets = new ArrayList<>();
  List<String> labels = new ArrayList<>();

  public TimedRoute(SortedMap<LocalDateTime, Map<String, Snapshot>> stored) {
    stored.keySet().forEach(date -> labels.add(date.toLocalTime().toString()));

    Map<String, List<Snapshot>> mapped = new LinkedHashMap<>();

    for (Map<String, Snapshot> snapshotMap : stored.values()) {
      snapshotMap.keySet().forEach(s -> mapped.putIfAbsent(s, new ArrayList<>()));
      for (Map.Entry<String, Snapshot> entry : snapshotMap.entrySet()) {
        mapped.get(entry.getKey()).add(entry.getValue());
      }
    }

    mapped.forEach((label, snapshots) -> {
      Dataset dataset = new Dataset();
      dataset.label = label;
      snapshots.forEach(s -> {
        double median = s.getMedian() / 1000D / 1000D;
        dataset.data.add(String.valueOf(median));
      });
      datasets.add(dataset);
    });

  }

  public List<Dataset> getDatasets() {
    return datasets;
  }

  public static class Dataset {
    String label;
    List<String> data = new ArrayList<>();

    public String getLabel() {
      return label;
    }

  }
}
