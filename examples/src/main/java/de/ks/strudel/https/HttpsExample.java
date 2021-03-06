/**
 * Copyright [2017] [Christian Loehnert]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ks.strudel.https;

import de.ks.strudel.Strudel;

public class HttpsExample {
  public static void main(final String[] args) throws Exception {
    Strudel strudel = Strudel.create();
    strudel.options().secure("/secure/keystore.jks", "password");
    strudel.get("/", (req, res) -> "Hello encryption!");
    strudel.start();
  }
}
