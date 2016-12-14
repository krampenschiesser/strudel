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
import de.ks.strudel.util.StrudelTestExtension;
import io.restassured.RestAssured;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(StrudelTestExtension.class)
public class WebsocketTest {
  private static final Logger log = LoggerFactory.getLogger(WebsocketTest.class);

  @Inject
  Strudel strudel;

  @Test
  void echo() throws Exception {
    strudel.websocket("/echo", new WebSocketConnectionCallback() {
      @Override
      public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        log.info("Got connection from {}", channel.getUrl());
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
          @Override
          protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            String data = message.getData();
            log.info("Received message {}", data);
            WebSockets.sendText("Server: " + data, channel, null);
          }
        });
        channel.resumeReceives();
      }
    });
    strudel.start();

    WebsocketClient client = new WebsocketClient(new URI("ws://localhost:" + RestAssured.port + "/echo"));
    client.connectBlocking();
    client.send("hello");
    String answer = client.getAnswer();
    assertEquals("Server: hello", answer);
  }

  static class WebsocketClient extends WebSocketClient {
    AtomicReference<String> answer = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);

    public WebsocketClient(URI serverURI) {
      super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
      log.info("Client opened");
    }

    @Override
    public void onMessage(String message) {
      log.info("Client received message {}", message);
      answer.set(message);
      latch.countDown();
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
      log.info("Client closed");
    }

    @Override
    public void onError(Exception ex) {
      log.error("Client got error ", ex);
    }

    public String getAnswer() throws InterruptedException {
      latch.await(5, TimeUnit.SECONDS);
      return answer.get();
    }
  }

//  class WebsocketClient {
//    public String sendAndGetMsg() throws Exception {
//      AtomicReference<String> answer = new AtomicReference<>();
//
//      final CountDownLatch messageLatch = new CountDownLatch(1);
//      final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();
//
//      ClientManager client = ClientManager.createClient();
//      client.connectToServer(new Endpoint() {
//        @Override
//        public void onOpen(javax.websocket.Session session, EndpointConfig config) {
//          try {
//            session.addMessageHandler((MessageHandler.Whole<String>) message -> {
//              answer.set(message);
//              messageLatch.countDown();
//            });
//            session.getBasicRemote().sendText("hello");
//          } catch (IOException e) {
//            log.error("Could not send message", e);
//          }
//        }
//      }, cec, new URI("ws://localhost:" + RestAssured.port + "/echo"));
//      messageLatch.await(4, TimeUnit.SECONDS);
//      return answer.get();
//    }
//  }
}
