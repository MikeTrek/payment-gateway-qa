package com.solidgate.api;

import com.solidgate.model.InitPaymentRequest;

import java.util.UUID;

/**
 * Reduces boilerplate when building InitPaymentRequest in tests.
 */
public final class PaymentRequestFactory {

  private static final String DEFAULT_EMAIL = "qa.test@example.com";
  private static final String DEFAULT_COUNTRY = "GBR";
  private static final String DEFAULT_SHOP = "Test Shop";
  private static final String DEFAULT_SUCCESS = "https://example.com/success";
  private static final String DEFAULT_FAIL = "https://example.com/fail";

  private PaymentRequestFactory() {
  }

  public static InitPaymentRequest create(int amount, String currency) {
    return create(amount, currency, "Test payment");
  }

  public static InitPaymentRequest create(int amount, String currency, String description) {
    return create(generateOrderId(), amount, currency, description);
  }

  public static InitPaymentRequest create(
      String orderId, int amount, String currency, String description) {
    return InitPaymentRequest.builder()
        .orderId(orderId)
        .amount(amount)
        .currency(currency)
        .orderDescription(description)
        .customerEmail(DEFAULT_EMAIL)
        .geoCountry(DEFAULT_COUNTRY)
        .publicName(DEFAULT_SHOP)
        .successUrl(DEFAULT_SUCCESS)
        .failUrl(DEFAULT_FAIL)
        .build();
  }

  public static String generateOrderId() {
    return "test_" + UUID.randomUUID().toString().substring(0, 8);
  }

  public static String generateOrderId(String prefix) {
    return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
  }
}
