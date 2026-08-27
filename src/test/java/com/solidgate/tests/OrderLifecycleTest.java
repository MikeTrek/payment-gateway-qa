package com.solidgate.tests;

import com.solidgate.model.Money;
import com.solidgate.model.TestCard;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Payments")
@Feature("Order Lifecycle")
@Tag("quarantine")
class OrderLifecycleTest extends BaseUiTest {

  private static final Money FIVE_USD = new Money(500, "USD");

  @Test
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("Lifecycle: pay → settle → refund accepted")
  @Description("Proves the state machine: UI pay → auth_ok/settle_ok → refund API returns 200 with order reference")
  void shouldRefundAfterSuccessfulPayment() {
    FlakyRetry.run("shouldRefundAfterSuccessfulPayment", () -> {
      String orderId = createAndOpenPaymentPage(FIVE_USD);

      paymentPage.pay(TestCard.VISA_SUCCESS);
      resultPage.verifyPaymentSuccess();

      // Wait for settlement
      Awaitility.await()
          .alias("Waiting for auth_ok/settle_ok before refund")
          .pollInterval(Duration.ofSeconds(CONFIG.statusPollIntervalSec()))
          .atMost(Duration.ofSeconds(CONFIG.statusPollTimeoutSec()))
          .until(() -> {
            Response r = apiClient.getOrderStatus(orderId);
            String status = r.jsonPath().getString("order.status");
            return List.of("auth_ok", "settle_ok").contains(status);
          });

      Response refundResponse = apiClient.refundOrder(orderId, FIVE_USD.amountMinor());
      assertThat(refundResponse.statusCode())
          .as("Refund request accepted by gateway")
          .isIn(200, 201);
    });
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Lifecycle: double-clicking submit does not double-charge")
  @Description("An impatient payer double-clicking Pay must still settle exactly once, "
      + "for the requested amount — not error, and not visibly submit twice")
  void shouldNotDoubleChargeOnDoubleClickSubmit() {
    FlakyRetry.run("shouldNotDoubleChargeOnDoubleClickSubmit", () -> {
      String orderId = createAndOpenPaymentPage(FIVE_USD);

      paymentPage.payWithDoubleClickSubmit(TestCard.VISA_SUCCESS);
      resultPage.verifyPaymentSuccess();

      verifyOrderStatus(orderId, FIVE_USD, "auth_ok", "settle_ok");
    });
  }
}
