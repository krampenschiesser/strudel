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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import de.ks.strudel.Strudel;
import de.ks.strudel.StrudelModule;
import de.ks.strudel.gson.GsonModule;
import de.ks.strudel.gson.GsonParser;
import de.ks.strudel.json.JsonParser;
import de.ks.strudel.metrics.avaje.DropwizardMetricModule;
import de.ks.strudel.template.TemplateEngine;
import de.ks.strudel.trimou.TrimouEngine;
import de.ks.strudel.trimou.TrimouModule;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * See: WEB-INF/template/metric/metrics.html
 */
public class MetricsExample {
  public static void main(final String[] args) {
    Injector injector = Guice.createInjector(new StrudelModule(), new MetricsExampleModule(), new DropwizardMetricModule(), new TrimouModule(), new GsonModule());
    Strudel strudel = injector.getInstance(Strudel.class);

    MetricsCollector metrics = injector.getInstance(MetricsCollector.class);
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(metrics::sample, 10, 10, TimeUnit.SECONDS);

    strudel.webjars();
    strudel.get("/", AllMetricsHandler.class).template();
    strudel.get("/random-time-out", (request, response) -> {
      Thread.sleep(ThreadLocalRandom.current().nextInt(1, 50));
      response.redirect("/");
      return "";
    }).async();
    strudel.get("/hello", (request, response) -> {
      response.redirect("/");
      return "hello";
    });
    strudel.get("/exception", (request, response) -> {
      response.redirect("/");
      throw new RuntimeException("An exception");
    });
    strudel.get("/timedroute", TimedRouteHandler.class).json();

    strudel.start();
  }

  static class MetricsExampleModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(TemplateEngine.class).to(TrimouEngine.class);
      bind(JsonParser.class).to(GsonParser.class);
    }
  }
}
