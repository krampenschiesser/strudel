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
package de.ks.strudel.metrics.avaje;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import de.ks.strudel.metrics.MetricsCallback;
import org.avaje.metric.core.DefaultMetricManager;
import org.avaje.metric.spi.PluginMetricManager;

import java.util.concurrent.TimeUnit;

public class AvajeMetricModule extends AbstractModule {
  public static final String METRIC_BUCKETS = "MetricBuckets";

  @Override
  protected void configure() {
    bind(PluginMetricManager.class).to(DefaultMetricManager.class).asEagerSingleton();
    bind(MetricsCallback.class).to(AvajeMetricCallback.class);

    bind(int[].class).annotatedWith(Names.named(METRIC_BUCKETS)).toInstance(new int[]{//
      (int) TimeUnit.MILLISECONDS.toNanos(1),//
      (int) TimeUnit.MILLISECONDS.toNanos(5),//
      (int) TimeUnit.MILLISECONDS.toNanos(10),//
      (int) TimeUnit.MILLISECONDS.toNanos(20),//
      (int) TimeUnit.MILLISECONDS.toNanos(50),//
      (int) TimeUnit.MILLISECONDS.toNanos(100),//
      (int) TimeUnit.MILLISECONDS.toNanos(500)//
    });
  }
}
