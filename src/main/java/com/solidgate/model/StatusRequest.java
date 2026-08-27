package com.solidgate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusRequest {

  @JsonProperty("order_id")
  private final String orderId;

  public StatusRequest(String orderId) {
    this.orderId = orderId;
  }

  public String toJson() {
    return JsonMapper.toJson(this);
  }
}
