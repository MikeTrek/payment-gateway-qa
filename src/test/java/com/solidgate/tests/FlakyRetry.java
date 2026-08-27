package com.solidgate.tests;

import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.WebDriverException;

/**
 * Retries a block of quarantined test logic when it fails for infrastructure reasons — a
 * dropped WebDriver session, a browser-launch hiccup, a sandbox timeout — but never when it
 * fails because an assertion was actually wrong. A real bug must never be laundered into a
 * green build by retrying it into passing.
 *
 * <p>Deliberately a plain method wrapping the whole test body, not a JUnit
 * {@code InvocationInterceptor}: JUnit Jupiter does not support re-invoking a test method's
 * {@code Invocation} more than once (it throws {@code JUnitException: Chain of
 * InvocationInterceptors called invocation multiple times instead of just once} as soon as
 * another interceptor — even the built-in {@code TimeoutExtension} — is in the chain). Wrapping
 * the body directly sidesteps that entirely: each retry is a fresh, ordinary method call.
 *
 * <p>Only {@link WebDriverException} and {@link ConditionTimeoutException} (Awaitility's
 * status-poll timeout) are treated as flakiness. Anything extending {@link AssertionError} —
 * including Selenide's own {@code UIAssertionError} and AssertJ failures — is rethrown
 * immediately on the first attempt.
 */
public final class FlakyRetry {

  static final int MAX_ATTEMPTS = 3;

  private FlakyRetry() {
  }

  public static void run(String label, Runnable action) {
    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      try {
        action.run();
        return;
      } catch (AssertionError e) {
        throw e;
      } catch (WebDriverException | ConditionTimeoutException e) {
        if (attempt == MAX_ATTEMPTS) {
          throw e;
        }
        System.out.printf(
            "[quarantine-retry] %s: attempt %d/%d failed with %s — retrying "
                + "(infrastructure flakiness, not an assertion failure)%n",
            label, attempt, MAX_ATTEMPTS, e.getClass().getSimpleName());
      }
    }
  }
}
