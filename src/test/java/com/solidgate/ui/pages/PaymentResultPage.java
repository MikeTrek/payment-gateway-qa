package com.solidgate.ui.pages;

import com.solidgate.config.ConfigProvider;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for the payment result after checkout.
 * Solidgate shows success/failure status on their hosted page.
 */
public class PaymentResultPage {

  private static final Duration RESULT_TIMEOUT =
      Duration.ofSeconds(ConfigProvider.config().paymentResultTimeoutSec());

  @Step("UI: Verify payment completed successfully")
  public void verifyPaymentSuccess() {
    $("[data-testid='status-title']")
        .shouldBe(visible, RESULT_TIMEOUT)
        .shouldHave(text("successful"));
    $("[data-testid='retryable-payment-error']")
        .shouldNot(exist);
  }

  @Step("UI: Verify payment error is displayed")
  public void verifyPaymentError() {
    $("[data-testid='retryable-payment-error']")
        .should(exist, RESULT_TIMEOUT);
  }
}
