package com.solidgate.api;

import com.solidgate.config.ConfigProvider;
import com.solidgate.model.StatusRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Payments")
@Feature("Signature & Auth")
class SignatureAndAuthTest {

  private static final String PUB = ConfigProvider.config().publicKey();
  private static final String SEC = ConfigProvider.config().secretKey();
  private static final String PAY_URL = ConfigProvider.config().payBaseUrl();

  @Test
  @DisplayName("SignatureGenerator produces deterministic output for same input")
  void shouldProduceDeterministicSignature() {
    String sig1 = SignatureGenerator.generate("pk", "{}", "sk");
    String sig2 = SignatureGenerator.generate("pk", "{}", "sk");

    assertThat(sig1).isEqualTo(sig2).isNotBlank();
  }

  @Test
  @DisplayName("Different body produces different signature")
  void shouldProduceDifferentSignatureForDifferentBody() {
    String sig1 = SignatureGenerator.generate("pk", "{\"a\":1}", "sk");
    String sig2 = SignatureGenerator.generate("pk", "{\"a\":2}", "sk");

    assertThat(sig1).isNotEqualTo(sig2);
  }

  @Test
  @DisplayName("Signature is valid Base64")
  void shouldProduceValidBase64() {
    String sig = SignatureGenerator.generate("pk", "{\"test\":true}", "sk");

    assertThat(sig).matches("^[A-Za-z0-9+/=]+$");
    byte[] decoded = java.util.Base64.getDecoder().decode(sig);
    // Decoded should be hex string (128 chars for SHA-512)
    assertThat(new String(decoded)).matches("^[0-9a-f]{128}$");
  }

  @Test
  @DisplayName("Invalid signature returns error response")
  void shouldRejectInvalidSignature() {
    String body = new StatusRequest("order_example").toJson();

    Response response = given()
        .contentType(JSON).accept(JSON)
        .baseUri(PAY_URL)
        .header("merchant", PUB)
        .header("signature", "aW52YWxpZA==")
        .body(body)
        .post("/status");

    assertThat(response.jsonPath().getString("error.code"))
        .as("Invalid signature should be reported as auth failure 1.01")
        .isEqualTo("1.01");
    assertThat(response.jsonPath().getList("error.messages", String.class))
        .as("Error should explain authentication failed")
        .contains("Authentication failed");
  }

  @Test
  @DisplayName("Wrong merchant header returns error response")
  void shouldRejectWrongMerchant() {
    String body = new StatusRequest("order_example").toJson();
    String sig = SignatureGenerator.generate("wrong_merchant", body, SEC);

    Response response = given()
        .contentType(JSON).accept(JSON)
        .baseUri(PAY_URL)
        .header("merchant", "wrong_merchant")
        .header("signature", sig)
        .body(body)
        .post("/status");

    assertThat(response.jsonPath().getString("error.code"))
        .as("Wrong merchant should be reported as auth failure 1.01")
        .isEqualTo("1.01");
    assertThat(response.jsonPath().getList("error.messages", String.class))
        .as("Error should explain authentication failed")
        .contains("Authentication failed");
  }
}
