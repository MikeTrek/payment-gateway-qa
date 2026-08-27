package com.solidgate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request body for POST /init (create payment page).
 * Structure: { "order": {...}, "page_customization": {...} }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitPaymentRequest {

  @JsonProperty("order")
  private Order order;

  @JsonProperty("page_customization")
  private PageCustomization pageCustomization;

  private InitPaymentRequest() {
  }

  public Order getOrder() {
    return order;
  }

  public String toJson() {
    return JsonMapper.toJson(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Order {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("order_description")
    private String orderDescription;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("geo_country")
    private String geoCountry;

    public String getOrderId() {
      return orderId;
    }

    public int getAmount() {
      return amount;
    }

    public String getCurrency() {
      return currency;
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class PageCustomization {
    @JsonProperty("public_name")
    private String publicName;

    @JsonProperty("order_title")
    private String orderTitle;

    @JsonProperty("order_description")
    private String orderDescription;

    @JsonProperty("success_url")
    private String successUrl;

    @JsonProperty("fail_url")
    private String failUrl;
  }

  public static class Builder {
    private final InitPaymentRequest request = new InitPaymentRequest();
    private final Order order = new Order();
    private final PageCustomization page = new PageCustomization();

    public Builder orderId(String orderId) {
      order.orderId = orderId;
      return this;
    }

    public Builder amount(int amount) {
      order.amount = amount;
      return this;
    }

    public Builder currency(String currency) {
      order.currency = currency;
      return this;
    }

    public Builder orderDescription(String description) {
      order.orderDescription = description;
      page.orderDescription = description;
      return this;
    }

    public Builder customerEmail(String email) {
      order.customerEmail = email;
      return this;
    }

    public Builder geoCountry(String country) {
      order.geoCountry = country;
      return this;
    }

    public Builder publicName(String name) {
      page.publicName = name;
      return this;
    }

    public Builder successUrl(String url) {
      page.successUrl = url;
      return this;
    }

    public Builder failUrl(String url) {
      page.failUrl = url;
      return this;
    }

    public InitPaymentRequest build() {
      if (page.publicName == null) {
        page.publicName = "Test Shop";
      }
      request.order = order;
      request.pageCustomization = page;
      return request;
    }
  }
}
