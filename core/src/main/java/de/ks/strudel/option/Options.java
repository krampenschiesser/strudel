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
package de.ks.strudel.option;

import javax.inject.Singleton;

@Singleton
public class Options {
  private int port = 4567;
  private String host = "0.0.0.0";

  /**
   * @return the configured host adress
   */
  public String host() {
    return host;
  }

  /**
   * Assigns the bind adress for starting the host on. Default is 0.0.0.0 which means all adresses.
   * This is important on a clustered environment when a server switch happens.
   *
   * @param host the adress
   * @return self
   */
  public Options host(String host) {
    this.host = host;
    return this;
  }

  public Options port(int port) {
    this.port = port;
    return this;
  }

  public int port() {
    return port;
  }

//  public Options secure(String keystoreFile, String keystorePassword, String truststoreFile, String truststorePassword) {
//    return this;
//  }
}
