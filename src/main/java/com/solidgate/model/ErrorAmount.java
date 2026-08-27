package com.solidgate.model;

/**
 * Solidgate sandbox amount-based error triggers.
 * Any card + this amount = specific error code.
 */
public enum ErrorAmount {

  AMT_0_01(1, "0.01", "General decline"),
  AMT_1_01(700, "1.01", "Authentication failed"),
  AMT_3_01(301, "3.01", "Card is blocked"),
  AMT_3_02(302, "3.02", "Insufficient funds"),
  AMT_3_02_ALT(3020, "3.02", "Insufficient funds (alt)"),
  AMT_3_04(304, "3.04", "Issuer declined"),
  AMT_3_05(305, "3.05", "Call your bank"),
  AMT_3_05_ALT(1600, "3.05", "Call your bank (alt)"),
  AMT_3_08(308, "3.08", "Do not honor"),
  AMT_3_08_ALT(3080, "3.08", "Do not honor (alt)"),
  AMT_3_10(310, "3.10", "Fraud suspected"),
  AMT_4_03(403, "4.03", "Pick up card"),
  AMT_5_02(502, "5.02", "Invalid card token"),
  AMT_5_03(1200, "5.03", "Application error"),
  AMT_5_12(512, "5.12", "Account is blocked");

  private final int amount;
  private final String errorCode;
  private final String description;

  ErrorAmount(int amount, String errorCode, String description) {
    this.amount = amount;
    this.errorCode = errorCode;
    this.description = description;
  }

  public int amount() {
    return amount;
  }

  public String errorCode() {
    return errorCode;
  }

  public String description() {
    return description;
  }
}
