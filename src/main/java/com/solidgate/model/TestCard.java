package com.solidgate.model;

import java.time.LocalDate;

/**
 * Solidgate sandbox test cards with known behavior.
 */
public enum TestCard {

  // === SUCCESS ===
  VISA_SUCCESS("4067429974719265", "123", "Visa success card"),
  MASTERCARD_SUCCESS("5361074849060618", "456", "Mastercard success card — void_failed scenario"),
  RECURRING_TOKEN("4532456618142692", "123", "Recurring token card"),

  // === AUTH / FLOW FAILURES ===
  AUTH_FAILED("4553815318053315", "123", "Auth failed"),
  VOID_FAILED("5361074849060618", "456", "Void failed scenario"),
  RECURRING_FAILED("4123212486997559", "123", "Recurring failed"),
  REFUND_FAILED("4476284533825961", "123", "Refund failed"),
  INCREMENTAL_FAILED("5140308911473822", "123", "Incremental auth failed"),

  // === ERROR CODES ===
  ERR_0_01_GENERAL_DECLINE("4510108818406882", "123", "0.01 General decline"),
  ERR_0_02_ORDER_EXPIRED("4532003312475364", "123", "0.02 Order expired"),
  ERR_0_03_ANTIFRAUD("4929213352238223", "123", "0.03 Antifraud"),
  ERR_2_06_INVALID_CVV("4838438060885557", "123", "2.06 Invalid CVV"),
  ERR_2_08_INVALID_CARD("4539146503757117", "123", "2.08 Invalid card number"),
  ERR_2_09_INVALID_EXPIRY("4945960509912296", "123", "2.09 Invalid expiry"),
  ERR_2_10_3DS_NOT_SHOWN("4857027008185133", "123", "2.10 3DS URL not displayed"),
  ERR_3_01_CARD_BLOCKED("5462413335551193", "123", "3.01 Card blocked"),
  ERR_3_02_INSUFFICIENT_FUNDS("5151948477715326", "123", "3.02 Insufficient funds"),
  ERR_3_03_LIMIT_EXCEEDED("4485664001324176", "123", "3.03 Transaction limit exceeded"),
  ERR_3_04_ISSUER_DECLINE("5361250317309261", "123", "3.04 Issuer declined"),
  ERR_3_05_DO_NOT_HONOR("4916242642369774", "123", "3.05 Do not honor"),
  ERR_3_06_DEBIT_DECLINE("5261820900437819", "123", "3.06 Debit card declined"),
  ERR_3_07_CARD_RESTRICTED("4347225319309917", "123", "3.07 Card restricted"),
  ERR_3_08_BANK_DECLINE("4907428874384745", "123", "3.08 Bank general decline"),
  ERR_3_10_FRAUD_SUSPECTED("4283184051091165", "123", "3.10 Fraud suspected by issuer"),
  ERR_4_02_STOLEN_CARD("4983102885450335", "123", "4.02 Stolen card"),
  ERR_4_04_LOST_CARD("4222192107639022", "123", "4.04 Lost card"),
  ERR_4_05_ACQUIRER_FRAUD("4075752033922822", "123", "4.05 Acquirer fraud block"),
  ERR_4_07_ANTIFRAUD_RULE("6011491463366455", "123", "4.07 Antifraud rule triggered"),
  ERR_5_01_PAYMENT_ERROR("5539974195624197", "123", "5.01 Payment error"),
  ERR_5_02_INVALID_MERCHANT("4485589319980072", "123", "5.02 Invalid merchant"),
  ERR_5_03_APP_ERROR("5414915934193648", "123", "5.03 Application error"),
  ERR_5_04_CONFIG_ERROR("5394262465415346", "123", "5.04 Merchant config error"),
  ERR_5_08_PROCESSING_ERROR("5199914302370491", "123", "5.08 Processing error"),
  ERR_6_02_CONNECTION("4935964870334207", "123", "6.02 Connection error"),
  ERR_7_01_TECHNICAL("5241981397484014", "123", "7.01 Technical error"),

  // === CARD BRANDS ===
  BRAND_VISA("4067429974719265", "123", "Visa"),
  BRAND_MASTERCARD("5329777445319300", "123", "Mastercard"),
  BRAND_MAESTRO("6763428189229070", "123", "Maestro"),
  BRAND_JCB("3527602488193781", "123", "JCB"),
  BRAND_DISCOVER("6011218207927015", "123", "Discover"),
  BRAND_UNIONPAY("6229261954246138", "123", "UnionPay"),
  BRAND_AMEX("371495481347626", "1234", "Amex"),
  BRAND_DINERS("36527094796869", "123", "Diners"),

  // === 3DS ===
  THREE_DS("497592770594980", "123", "3D Secure verification"),

  // === RECURRING VARIANTS FOR ERROR CARDS ===
  ERR_3_02_RECURRING("4890838637940261", "123", "3.02 Insufficient funds — recurring"),
  ERR_3_04_RECURRING("2490161849593101", "123", "3.04 Issuer declined — recurring"),
  ERR_3_07_RECURRING("5134431550984251", "123", "3.07 Card restricted — recurring"),
  ERR_3_10_RECURRING("4423511615594071", "123", "3.10 Fraud suspected — recurring"),

  // === RELOADABLE PREPAID INDICATOR ===
  RELOADABLE_SUCCESS("5469570002120009", "123", "Reloadable prepaid — success"),
  RELOADABLE_RECURRING_DECLINE("5061050254756707864", "123", "Reloadable prepaid — recurring decline"),
  RELOADABLE_DECLINED("5503981533124642", "123", "Reloadable prepaid — declined");

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
