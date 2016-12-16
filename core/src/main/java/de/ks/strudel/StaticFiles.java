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
package de.ks.strudel;

import de.ks.strudel.route.RouteBuilder;

public class StaticFiles {
  private final String location;
  private final String url;
  private RouteBuilder routeBuilder;

  public StaticFiles(String location, String url) {
    this.location = location.startsWith("/") ? location.substring(1) : location;
    this.url = url;
  }

//  public StaticFiles cached() {
//    return this;
//  }
//
//  public StaticFiles header(String key, String value) {
//    return this;
//  }

  public String getLocation() {
    return location;
  }

  public String getUrl() {
    return url;
  }

  StaticFiles setRouteBuilder(RouteBuilder routeBuilder) {
    this.routeBuilder = routeBuilder;
    return this;
  }

  public RouteBuilder getRouteBuilder() {
    return routeBuilder;
  }
}
