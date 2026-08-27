package com.solidgate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefundRequest {

  @JsonProperty("order_id")
  private final String orderId;

  @JsonProperty("amount")
  private final int amount;

  public RefundRequest(String orderId, int amount) {
    this.orderId = orderId;
    this.amount = amount;
  }

  public String toJson() {
    return JsonMapper.toJson(this);
  }
}
