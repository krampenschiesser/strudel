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
import de.ks.strudel.Handler;
import de.ks.strudel.Response;
import de.ks.strudel.metrics.avaje.DropWizardMetricCallback;
import de.ks.strudel.metrics.model.Route;
import de.ks.strudel.request.Request;
import de.ks.strudel.template.ModelAndView;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllMetricsHandler implements Handler {
  final MetricRegistry metricRegistry;

  @Inject
  public AllMetricsHandler(MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    Map<Object, Object> model = new HashMap<>();
    model.put("unknownroutes", getUnkownRoutes());
    model.put("knownroutes", getKownRoutes());
    model.put("title", "Hallo Metrics!");
    return new ModelAndView(model, "metric/metrics.html");
  }

  public List<Route> getUnkownRoutes() {
    ArrayList<Route> routes = new ArrayList<>();
    metricRegistry.getCounters((name, metric) -> name.startsWith(DropWizardMetricCallback.UNKNOWNROUTE_PREFIX))//
                  .forEach((n, c) -> routes.add(new Route(n.substring(DropWizardMetricCallback.UNKNOWNROUTE_PREFIX.length()), c.getCount())));
    return routes;
  }

  public List<Route> getKownRoutes() {
    ArrayList<Route> routes = new ArrayList<>();
    metricRegistry.getCounters((name, metric) -> name.startsWith(DropWizardMetricCallback.ROUTE_PREFIX))//
                  .forEach((n, c) -> routes.add(new Route(n.substring(DropWizardMetricCallback.ROUTE_PREFIX.length()), c.getCount())));
    return routes;
  }
}
