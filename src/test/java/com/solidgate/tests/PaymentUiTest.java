package com.solidgate.tests;

import com.solidgate.model.Money;
import com.solidgate.model.TestCard;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Epic("Payments")
@Feature("Card Payment E2E")
class PaymentUiTest extends BaseUiTest {

  private static final Money TEN_99_USD = new Money(1099, "USD");

  /**
   * Assignment Test 1 + Test 2 in one flow: create a Payment Page for a fresh order, pay it
   * through the UI, verify the success page, then check /status — amount and currency must
   * match what the Payment Page displayed, and the payment must have a successful status.
   */
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
}
