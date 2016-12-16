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
package de.ks.strudel.server;

import de.ks.strudel.option.Options;
import de.ks.strudel.route.Router;
import io.undertow.Undertow;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Used to start/stop and configure undertow
 */
@Singleton
public class ServerManager {
  final Options options;
  final Router router;

  final AtomicReference<Undertow> undertowRef = new AtomicReference<>();

  @Inject public ServerManager(Options options, Router router) {
    this.options = options;
    this.router = router;
  }

  public boolean isStarted() {
    return undertowRef.get() != null;
  }

  public synchronized void start() {
    if (undertowRef.get() == null) {
      int port = options.port();
      String host = options.host();

      Undertow undertow = Undertow.builder()//
                                  .addHttpListener(port, host)//
                                  .setHandler(router.getHandler())//
                                  .build();
      undertow.start();
      undertowRef.compareAndSet(null, undertow);
    }
  }

  public synchronized void stop() {
    if (undertowRef.get() != null) {
      undertowRef.get().stop();
      undertowRef.set(null);
    }
  }

  public synchronized void restart() {
    stop();
    start();
  }

  public Undertow getUndertow() {
    return undertowRef.get();
  }
}
