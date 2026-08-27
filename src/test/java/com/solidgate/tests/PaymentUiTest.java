package com.solidgate.tests;

import com.solidgate.model.Money;
import com.solidgate.model.TestCard;
import com.solidgate.ui.pages.ThreeDsChallengePage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Epic("Payments")
@Feature("Card Payment E2E")
class PaymentUiTest extends BaseUiTest {

  private static final Money TEN_99_USD = new Money(1099, "USD");
  private static final Money TWENTY_FIVE_EUR = new Money(2500, "EUR");

  @Test
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("E2E: Visa payment — create page, pay, verify status")
  @Description("Full cycle: API /init → UI card payment → verify success → poll /status for settle_ok")
  void shouldCompleteVisaPaymentAndVerifyStatus() {
    String orderId = createAndOpenPaymentPage(TEN_99_USD);

    paymentPage.pay(TestCard.VISA_SUCCESS);
    resultPage.verifyPaymentSuccess();

    verifyOrderStatus(orderId, TEN_99_USD, "auth_ok", "settle_ok");
  }

  @Test
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("E2E: Mastercard payment — create page, pay, verify status")
  @Description("Full cycle with Mastercard: API /init → UI → success → /status settle_ok")
  void shouldCompleteMastercardPaymentAndVerifyStatus() {
    String orderId = createAndOpenPaymentPage(TWENTY_FIVE_EUR);

    paymentPage.pay(TestCard.BRAND_MASTERCARD);
    resultPage.verifyPaymentSuccess();

    verifyOrderStatus(orderId, TWENTY_FIVE_EUR, "auth_ok", "settle_ok");
  }

  @Test
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("E2E: Declined card — verify error UI + auth_failed status")
  @Description("Card triggers auth_failed → error on page → /status = auth_failed")
  void shouldShowErrorAndReturnAuthFailedStatus() {
    String orderId = createAndOpenPaymentPage(TEN_99_USD);

    paymentPage.pay(TestCard.AUTH_FAILED);
    resultPage.verifyPaymentError();

    verifyOrderStatus(orderId, TEN_99_USD, "auth_failed");
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("E2E: General decline — error on UI + declined status")
  @Description("Card triggers general decline 0.01 → error shown → /status = declined")
  void shouldShowErrorForGeneralDecline() {
    String orderId = createAndOpenPaymentPage(TEN_99_USD);

    paymentPage.pay(TestCard.ERR_0_01_GENERAL_DECLINE);
    resultPage.verifyPaymentError();

    verifyOrderStatus(orderId, TEN_99_USD, "auth_failed", "declined");
  }

  @Test
  @Tag("quarantine")
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("E2E: 3D Secure challenge — redirect, verify, complete payment")
  @Description("3DS card triggers ACS challenge iframe → enter code → return to success → /status auth_ok")
  void shouldComplete3dsChallenge() {
    FlakyRetry.run("shouldComplete3dsChallenge", () -> {
      String orderId = createAndOpenPaymentPage(TEN_99_USD);

      paymentPage.pay(TestCard.THREE_DS);

      ThreeDsChallengePage challengePage = new ThreeDsChallengePage();
      challengePage.switchToChallengeFrame()
          .completeChallenge();

      resultPage.verifyPaymentSuccess();
      verifyOrderStatus(orderId, TEN_99_USD, "auth_ok", "settle_ok");
    });
  }
}
