package de.ks.strudel.metrics;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHistoryTest {
  @Test
  void exceptionCounting() {
    RuntimeException first = new RuntimeException("huhu");
    RuntimeException other = new RuntimeException("other");
    RuntimeException otherClass = new IllegalArgumentException("otherclass");

    ExceptionHistory exceptionHistory = new ExceptionHistory();
    exceptionHistory.trackException(first);
    exceptionHistory.trackException(first);
    exceptionHistory.trackException(other);
    exceptionHistory.trackException(otherClass);

    int size = exceptionHistory.getExceptions().size();
    assertEquals(3, size);

    StoredException storedException = exceptionHistory.getExceptions().stream().filter(s -> s.getCount() > 1).findFirst().get();
    assertEquals(2, storedException.getCount());
  }

  @Test
  void removeOldest() throws InterruptedException {
    RuntimeException first = new RuntimeException("first");
    RuntimeException second = new RuntimeException("second");
    RuntimeException third = new RuntimeException("third");

    ExceptionHistory exceptionHistory = new ExceptionHistory(2);
    exceptionHistory.trackException(first);
    Thread.sleep(1);
    exceptionHistory.trackException(second);
    Thread.sleep(1);
    exceptionHistory.trackException(first);//track again update age
    Thread.sleep(1);
    exceptionHistory.trackException(third);//now we have too many, we remove the oldest

    assertEquals(2, exceptionHistory.getExceptions().size());

    Optional<StoredException> found = exceptionHistory.getExceptions().stream().filter(s -> s.getException().getMessage().equals("second")).findAny();
    assertFalse(found.isPresent());
  }

  @Test
  void nullMessage() {
    new ExceptionHistory().trackException(new RuntimeException((String) null));
  }
}