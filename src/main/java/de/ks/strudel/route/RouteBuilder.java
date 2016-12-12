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
package de.ks.strudel.route;

import de.ks.strudel.Handler;

public class RouteBuilder {

  String path;
  Handler handler;
  HttpMethod method;
  boolean gzip;
  FilterType filterType;

  public RouteBuilder path(String path) {
    this.path = path;
    return this;
  }

  public RouteBuilder handler(Handler handler) {
    this.handler = handler;
    return this;
  }

  public RouteBuilder method(HttpMethod method) {
    this.method = method;
    return this;
  }

  public RouteBuilder get() {
    return method(HttpMethod.GET);
  }

  public RouteBuilder put() {
    return method(HttpMethod.PUT);
  }

  public RouteBuilder post() {
    return method(HttpMethod.POST);
  }

  public RouteBuilder delete() {
    return method(HttpMethod.DELETE);
  }

  public RouteBuilder async() {
    return this;
  }

  public RouteBuilder sync() {
    return this;
  }

  public RouteBuilder gzip(boolean enable) {
    this.gzip = enable;
    return this;
  }

  public RouteBuilder filter(FilterType filterType) {
    this.filterType = filterType;
    return this;
  }

  public Route build() {
    return new Route(this);
  }
}
