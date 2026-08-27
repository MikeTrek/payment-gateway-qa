package com.solidgate.api;

import com.solidgate.config.ConfigProvider;
import com.solidgate.model.InitPaymentRequest;
import com.solidgate.model.StatusRequest;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class SolidgateApiClient {

  private static final AllureRestAssured ALLURE_FILTER = new AllureRestAssured();

  private final String publicKey;
  private final String secretKey;
  private final String paymentPageBaseUrl;
  private final String payBaseUrl;

  public SolidgateApiClient() {
    this.publicKey = ConfigProvider.config().publicKey();
    this.secretKey = ConfigProvider.config().secretKey();
    this.paymentPageBaseUrl = ConfigProvider.config().paymentPageBaseUrl();
    this.payBaseUrl = ConfigProvider.config().payBaseUrl();
  }

  @Step("API: Create payment page for order {request.order.orderId}")
  public Response createPaymentPage(InitPaymentRequest request) {
    String jsonBody = request.toJson();
    String signature = SignatureGenerator.generate(publicKey, jsonBody, secretKey);

    return baseRequest()
        .baseUri(paymentPageBaseUrl)
        .header("merchant", publicKey)
        .header("signature", signature)
        .body(jsonBody)
        .post("/init");
  }

  @Step("API: Get order status for orderId={orderId}")
  public Response getOrderStatus(String orderId) {
    StatusRequest statusRequest = new StatusRequest(orderId);
    String jsonBody = statusRequest.toJson();
    String signature = SignatureGenerator.generate(publicKey, jsonBody, secretKey);

    return baseRequest()
        .baseUri(payBaseUrl)
        .header("merchant", publicKey)
        .header("signature", signature)
        .body(jsonBody)
        .post("/status");
  }

  private RequestSpecification baseRequest() {
    return given()
        .filter(ALLURE_FILTER)
        .contentType(JSON)
        .accept(JSON);
  }
}
