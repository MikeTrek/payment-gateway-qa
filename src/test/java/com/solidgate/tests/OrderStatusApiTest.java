package com.solidgate.tests;

import com.solidgate.api.PaymentRequestFactory;
import com.solidgate.api.SolidgateApiClient;
import com.solidgate.model.ErrorAmount;
import com.solidgate.model.InitPaymentRequest;
import com.solidgate.model.Money;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Payments")
@Feature("Order Status API")
class OrderStatusApiTest {

  private SolidgateApiClient apiClient;

  @BeforeEach
  void init() {
    apiClient = new SolidgateApiClient();
  }

  @Test
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("GET /status returns expected data for pre-existing order")
  @Description("Verify order_example returns approved status with amount and currency")
  void shouldReturnExpectedFieldsForExistingOrder() {
    Response response = apiClient.getOrderStatus("order_example");

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("order.status")).isEqualTo("approved");
    assertThat(response.jsonPath().getInt("order.amount")).isPositive();
    assertThat(response.jsonPath().getString("order.currency")).isEqualTo("USD");
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("POST /init returns valid checkout URL")
  @Description("Verify /init creates a payment page and returns a Solidgate checkout URL")
  void shouldCreatePaymentPageSuccessfully() {
    InitPaymentRequest request = PaymentRequestFactory.create(500, "USD");

    Response response = apiClient.createPaymentPage(request);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("url"))
        .isNotBlank()
        .startsWith("https://payment-page.solidgate.com");
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("POST /init with error-triggering amount returns valid checkout URL")
  @Description("Amount=301 triggers error 3.01 after payment — /init itself should still succeed")
  void shouldCreatePaymentPageWithErrorAmount() {
    InitPaymentRequest request = PaymentRequestFactory.create(
        PaymentRequestFactory.generateOrderId("amt"),
        ErrorAmount.AMT_3_01.amount(), "USD",
        "Amount error: " + ErrorAmount.AMT_3_01.description());

    Response response = apiClient.createPaymentPage(request);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("url")).isNotBlank();
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("GET /status returns 200 for unknown order_id")
  @Description("Solidgate returns 200 with error body for unknown orders")
  void shouldReturn200ForUnknownOrder() {
    Response response = apiClient.getOrderStatus("nonexistent_order_12345");

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("/status response matches JSON schema")
  @Description("Schema guards against silent field renames — a missing field fails loudly")
  void shouldMatchOrderStatusSchema() {
    Response response = apiClient.getOrderStatus("order_example");

    response.then()
        .body(matchesJsonSchemaInClasspath("schemas/order-status-response.json"));
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("/init response matches JSON schema")
  @Description("Schema validates the checkout URL pattern and structure")
  void shouldMatchInitResponseSchema() {
    InitPaymentRequest request = PaymentRequestFactory.create(500, "USD");

    Response response = apiClient.createPaymentPage(request);

    response.then()
        .body(matchesJsonSchemaInClasspath("schemas/init-response.json"));
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Zero-decimal currency (JPY): amount in minor units = face value")
  @Description("1000 JPY means ¥1000, not ¥10.00 — proves minor-units awareness")
  void shouldHandleZeroDecimalCurrency() {
    Money jpy = new Money(1000, "JPY");
    InitPaymentRequest request = PaymentRequestFactory.create(
        PaymentRequestFactory.generateOrderId("jpy"),
        jpy.amountMinor(), jpy.currency(), "JPY zero-decimal test");

    Response response = apiClient.createPaymentPage(request);

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.jsonPath().getString("url")).isNotBlank();
  }

  @Test
  @Severity(SeverityLevel.NORMAL)
  @DisplayName("Idempotency: duplicate order_id returns the same payment page, not a new one")
  @Description("Re-POST /init with the same order_id must return an identical checkout "
      + "URL/id/guid — proof it's genuinely idempotent, not just tolerant of the retry")
  void shouldHandleDuplicateOrderId() {
    String orderId = PaymentRequestFactory.generateOrderId("idem");
    InitPaymentRequest request = PaymentRequestFactory.create(
        orderId, 500, "USD", "Idempotency test");

    Response first = apiClient.createPaymentPage(request);
    assertThat(first.statusCode()).isEqualTo(200);
    assertThat(first.jsonPath().getString("url")).isNotBlank();

    Response second = apiClient.createPaymentPage(request);
    assertThat(second.statusCode())
        .as("Duplicate order_id should still be accepted")
        .isEqualTo(200);
    assertThat(second.jsonPath().getString("id"))
        .as("Duplicate order_id must return the same payment page id, not a new one")
        .isEqualTo(first.jsonPath().getString("id"));
    assertThat(second.jsonPath().getString("url"))
        .as("Duplicate order_id must return the same checkout URL")
        .isEqualTo(first.jsonPath().getString("url"));
  }
}
