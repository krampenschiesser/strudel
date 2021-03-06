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
package de.ks.strudel.route.handler;

import de.ks.strudel.HandlerNoReturn;
import io.undertow.util.CopyOnWriteMap;

import javax.inject.Singleton;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Stores handlers for exceptions.
 * Each handler is a supplier which should generate a new instance of the exception handler.
 */
@Singleton
public class ExceptionMappingRegistry {
  Map<Class<? extends Exception>, Supplier<HandlerNoReturn>> exceptionMappings = new CopyOnWriteMap<>();

  public Map<Class<? extends Exception>, Supplier<HandlerNoReturn>> getExceptionMappings() {
    return exceptionMappings;
  }
}
