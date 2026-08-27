package com.solidgate.model;

import java.util.Objects;
import java.util.Set;

/**
 * Minor-units money type. Prevents the bug class where amount and currency travel separately.
 * {@code new Money(1099, "USD")} means $10.99;
 * {@code new Money(1000, "JPY")} means ¥1000 (JPY has zero decimal places).
 */
public final class Money {

  private static final Set<String> ZERO_DECIMAL_CURRENCIES =
      Set.of("JPY", "KRW", "VND", "BIF", "CLP", "DJF", "GNF", "ISK",
          "KMF", "PYG", "RWF", "UGX", "VUV", "XAF", "XOF", "XPF");

  private final int amountMinor;
  private final String currency;

  public Money(int amountMinor, String currency) {
    if (currency == null || currency.isBlank()) {
      throw new IllegalArgumentException("Currency must not be blank");
    }
    this.amountMinor = amountMinor;
    this.currency = currency.toUpperCase();
  }

  public int amountMinor() {
    return amountMinor;
  }

  public String currency() {
    return currency;
  }

  public boolean isZeroDecimal() {
    return ZERO_DECIMAL_CURRENCIES.contains(currency);
  }

  /**
   * Returns human-readable display string, e.g. "$10.99" or "¥1000".
   */
  public String display() {
    if (isZeroDecimal()) {
      return amountMinor + " " + currency;
    }
    return String.format("%.2f %s", amountMinor / 100.0, currency);
  }

  /**
   * Returns the amount in major units (e.g. dollars, not cents), respecting
   * zero-decimal currencies. Used to compare against amounts rendered on-screen
   * (the Payment Page shows major units, the API deals in minor units).
   */
  public java.math.BigDecimal majorUnits() {
    if (isZeroDecimal()) {
      return java.math.BigDecimal.valueOf(amountMinor);
    }
    return java.math.BigDecimal.valueOf(amountMinor, 2);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Money other)) {
      return false;
    }
    return amountMinor == other.amountMinor && currency.equals(other.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amountMinor, currency);
  }

  @Override
  public String toString() {
    return "Money{" + amountMinor + " " + currency + "}";
  }
}
