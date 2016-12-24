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

import com.codahale.metrics.Snapshot;
import de.ks.strudel.Handler;
import de.ks.strudel.Response;
import de.ks.strudel.metrics.model.TimedRoute;
import de.ks.strudel.request.Request;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.SortedMap;

public class TimedRouteHandler implements Handler {
  private final MetricsCollector metricsCollector;

  @Inject
  public TimedRouteHandler(MetricsCollector metricsCollector) {
    this.metricsCollector = metricsCollector;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    SortedMap<LocalDateTime, Map<String, Snapshot>> stored = metricsCollector.getStored();
    TimedRoute timedRoute = new TimedRoute(stored);
    return timedRoute;
  }
}
