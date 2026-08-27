package com.solidgate.tests;

import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriverException;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit-tests {@link FlakyRetry}'s policy directly — no browser, no JUnit extension machinery,
 * deterministic and fast. Proves the one property that actually matters: infrastructure
 * flakiness gets retried, assertion failures never do.
 */
class FlakyRetryTest {

  @Test
  void retriesOnWebDriverExceptionAndEventuallySucceeds() {
    AtomicInteger calls = new AtomicInteger();

    FlakyRetry.run("flaky-then-success", () -> {
      if (calls.incrementAndGet() < FlakyRetry.MAX_ATTEMPTS) {
        throw new WebDriverException("simulated dropped session");
      }
    });

    assertThat(calls.get())
        .as("should have retried until the final attempt succeeded")
        .isEqualTo(FlakyRetry.MAX_ATTEMPTS);
  }

  @Test
  void retriesOnAwaitilityTimeoutAndEventuallySucceeds() {
    AtomicInteger calls = new AtomicInteger();

    FlakyRetry.run("timeout-then-success", () -> {
      if (calls.incrementAndGet() < FlakyRetry.MAX_ATTEMPTS) {
        throw new ConditionTimeoutException("simulated status-poll timeout");
      }
    });

    assertThat(calls.get()).isEqualTo(FlakyRetry.MAX_ATTEMPTS);
  }

  @Test
  void exhaustsRetriesAndRethrowsPersistentWebDriverException() {
    AtomicInteger calls = new AtomicInteger();

    assertThatThrownBy(() -> FlakyRetry.run("always-flaky", () -> {
      calls.incrementAndGet();
      throw new WebDriverException("session never recovers");
    })).isInstanceOf(WebDriverException.class);

    assertThat(calls.get())
        .as("should have attempted exactly MAX_ATTEMPTS times, no more")
        .isEqualTo(FlakyRetry.MAX_ATTEMPTS);
  }

  @Test
  void neverRetriesAnAssertionFailure() {
    AtomicInteger calls = new AtomicInteger();

    assertThatThrownBy(() -> FlakyRetry.run("real-bug", () -> {
      calls.incrementAndGet();
      throw new AssertionError("amount did not match — a real bug, not flakiness");
    })).isInstanceOf(AssertionError.class);

    assertThat(calls.get())
        .as("an assertion failure must fail fast on the first attempt, never retried")
        .isEqualTo(1);
  }
}
