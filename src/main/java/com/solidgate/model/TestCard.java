package com.solidgate.model;

import java.time.LocalDate;

/**
 * Solidgate sandbox test card used by the E2E test. Per the assignment, only the card
 * number matters for the outcome — expiry/CVV just need to be well-formed.
 */
public enum TestCard {

  VISA_SUCCESS("4067429974719265", "123", "Visa success card");

  private final String number;
  private final String cvv;
  private final String description;

  TestCard(String number, String cvv, String description) {
    this.number = number;
    this.cvv = cvv;
    this.description = description;
  }

  public String number() {
    return number;
  }

  public String cvv() {
    return cvv;
  }

  public String description() {
    return description;
  }

  public String expiry() {
    LocalDate future = LocalDate.now().plusYears(3);
    return String.format("%02d/%02d", future.getMonthValue(), future.getYear() % 100);
  }
}
