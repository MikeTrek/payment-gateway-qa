package com.solidgate.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Payments")
@Feature("Webhook Signature Verification")
class WebhookSignatureTest {

  private static final String WH_PK = "wh_pk_test";
  private static final String WH_SK = "wh_sk_test_secret";

  @Test
  @DisplayName("Valid webhook signature verifies successfully")
  void shouldVerifyValidSignature() {
    String body = "{\"order_id\":\"ord_123\",\"status\":\"settle_ok\"}";
    String signature = WebhookSignatureVerifier.computeSignature(WH_PK, body, WH_SK);

    assertThat(WebhookSignatureVerifier.verify(WH_PK, body, WH_SK, signature))
        .as("Correctly signed webhook must verify")
        .isTrue();
  }

  @Test
  @DisplayName("Tampered body fails verification")
  void shouldRejectTamperedBody() {
    String original = "{\"order_id\":\"ord_123\",\"status\":\"settle_ok\"}";
    String tampered = "{\"order_id\":\"ord_123\",\"status\":\"refunded\"}";
    String signature = WebhookSignatureVerifier.computeSignature(WH_PK, original, WH_SK);

    assertThat(WebhookSignatureVerifier.verify(WH_PK, tampered, WH_SK, signature))
        .as("Tampered body must fail verification")
        .isFalse();
  }

  @Test
  @DisplayName("Wrong secret key fails verification")
  void shouldRejectWrongSecret() {
    String body = "{\"order_id\":\"ord_456\"}";
    String signature = WebhookSignatureVerifier.computeSignature(WH_PK, body, WH_SK);

    assertThat(WebhookSignatureVerifier.verify(WH_PK, body, "wrong_secret", signature))
        .as("Wrong secret must fail verification")
        .isFalse();
  }

  @Test
  @DisplayName("Missing signature fails closed instead of throwing")
  void shouldFailClosedOnMissingSignature() {
    String body = "{\"order_id\":\"ord_789\"}";

    assertThat(WebhookSignatureVerifier.verify(WH_PK, body, WH_SK, null))
        .as("A null signature (e.g. missing header) must be rejected, not crash the handler")
        .isFalse();
  }

  @Test
  @DisplayName("Webhook verifier uses same algorithm as SignatureGenerator")
  void shouldMatchSignatureGeneratorOutput() {
    String pk = "merchant_pk";
    String sk = "merchant_sk";
    String body = "{\"order_id\":\"test\"}";

    String fromGenerator = SignatureGenerator.generate(pk, body, sk);
    String fromVerifier = WebhookSignatureVerifier.computeSignature(pk, body, sk);

    assertThat(fromVerifier)
        .as("Both classes implement the same HMAC-SHA512 algorithm")
        .isEqualTo(fromGenerator);
  }
}
