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
package de.ks.strudel.handler;

import de.ks.strudel.Handler;
import de.ks.strudel.Request;
import de.ks.strudel.Response;
import de.ks.strudel.StaticFiles;
import io.undertow.Handlers;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;

public abstract class StaticFileHandler implements Handler {
  private StaticFiles staticFiles;
  private ResourceHandler resource;

  public void setStaticFileConfig(StaticFiles staticFiles) {
    this.staticFiles = staticFiles;

    ResourceManager resourceManager = createResourceManager(staticFiles);
//    CachingResourceManager cached = new CachingResourceManager(500, 500000, new DirectBufferCache(100, 100, 500000), resourceManager, -1);
    resource = Handlers.resource(resourceManager);
  }

  protected abstract ResourceManager createResourceManager(StaticFiles staticFiles);

  @Override
  public Object handle(Request request, Response response) throws Exception {
    String url = staticFiles.getUrl();

    String relativePath = request.getExchange().getRelativePath();
    if (relativePath.startsWith(url)) {
      relativePath = relativePath.substring(url.length());
    }
    request.getExchange().setRelativePath(relativePath);
    resource.handleRequest(request.getExchange());
    return null;
  }
}
