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
package de.ks.strudel.websocket;

import de.ks.strudel.Strudel;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import java.io.IOException;

public class EchoServer {
  public static void main(final String[] args) {
    Strudel strudel = Strudel.create();
    strudel.classpathLocation("/WEB-INF/websocket/", "/websocket");
    strudel.get("/", (request, response) -> response.redirect("/websocket/echo.html"));
    strudel.websocket("/echo", null, Listener::new);
    strudel.start();
  }

  private static class Listener extends AbstractReceiveListener {
    @Override
    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
      WebSockets.sendText("Server says: " + message.getData(), channel, null);
    }
  }
}
