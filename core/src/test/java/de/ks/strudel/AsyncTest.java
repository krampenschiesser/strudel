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

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(StrudelTestExtension.class)
public class AsyncTest {
  @Inject
  Strudel strudel;
  private ExecutorService executorService;

  @BeforeEach
  void setUp() {
    executorService = Executors.newCachedThreadPool();
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    executorService.shutdown();
    executorService.awaitTermination(10, TimeUnit.SECONDS);
  }

  @Test
  void asyncExecution() throws ExecutionException, InterruptedException {
    CountDownLatch latch = new CountDownLatch(10);

    strudel.get("/", (request, response) -> {
      latch.countDown();
      latch.await();
      return "test";
    }).async();
    strudel.start();

    ArrayList<CompletableFuture<Response>> futures = new ArrayList<>();
    for (int i = 0; i < 9; i++) {
      CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> RestAssured.get("/"), executorService);
      futures.add(future);
    }
    for (CompletableFuture<Response> future : futures) {
      assertFalse(future.isDone(), "The server send an answer although it should still wait!");
    }

    io.restassured.response.Response response = RestAssured.get("/");
    assertEquals("test", response.body().asString());
    for (CompletableFuture<Response> future : futures) {
      assertTimeout(Duration.ofSeconds(2), () -> {
        future.join();
      });
      assertEquals("test", future.get().body().asString());
    }
  }

  @Test
  void asyncBeforeAfter() {
    AtomicInteger count = new AtomicInteger();
    strudel.get("/", (request, response) -> {
      return "test";
    }).async((request, response) -> count.incrementAndGet(), (request, response) -> count.incrementAndGet());
    strudel.start();
    io.restassured.response.Response response = RestAssured.get("/");
    assertEquals("test", response.body().asString());
    assertEquals(2, count.get(), "Callbacks have not been executed!");
  }
}
