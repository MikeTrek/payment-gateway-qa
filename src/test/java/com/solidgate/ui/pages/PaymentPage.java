package com.solidgate.ui.pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.solidgate.config.ConfigProvider;
import com.solidgate.model.TestCard;
import io.qameta.allure.Step;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object for Solidgate hosted payment page.
 */
public class PaymentPage {

  private static final Duration LOAD_TIMEOUT =
      Duration.ofSeconds(ConfigProvider.config().pageLoadTimeoutSec());

  private final SelenideElement cardNumberInput = $("[data-testid='cardNumber']");
  private final SelenideElement expiryInput = $("[data-testid='cardExpiryDate']");
  private final SelenideElement cvvInput = $("[data-testid='cardCvv']");
  private final SelenideElement submitButton = $("[data-testid='submit']");
  private final SelenideElement amountDisplay = $("[data-testid='price_major']");

  private static final Map<String, String> CURRENCY_SYMBOLS = Map.of(
      "$", "USD",
      "€", "EUR",
      "£", "GBP",
      "¥", "JPY"
  );
  private static final Pattern ISO_CODE_PATTERN = Pattern.compile("\\b([A-Z]{3})\\b");

  @Step("UI: Read displayed amount from Payment Page")
  public String getDisplayedAmount() {
    return amountDisplay.shouldBe(visible, LOAD_TIMEOUT).getText();
  }

  /**
   * Parses the displayed price into a major-units {@link BigDecimal} (e.g. "$10.99" -> 10.99),
   * so it can be compared against {@code Money.majorUnits()} from the /status response.
   */
  @Step("UI: Read displayed amount as a numeric value")
  public BigDecimal getDisplayedAmountValue() {
    return parseAmount(getDisplayedAmount());
  }

  /**
   * Best-effort extraction of the currency implied by the displayed price text — either
   * an embedded ISO code ("10.99 USD") or a known currency symbol ("$10.99").
   * Empty if the Payment Page renders neither (e.g. amount-only display).
   */
  @Step("UI: Read displayed currency from Payment Page")
  public Optional<String> getDisplayedCurrency() {
    return parseCurrency(getDisplayedAmount());
  }

  static BigDecimal parseAmount(String rawText) {
    String cleaned = rawText.replaceAll("[^0-9.,]", "");
    if (cleaned.contains(".") && cleaned.contains(",")) {
      // Whichever separator appears last is the decimal separator; the other is a thousands grouping.
      cleaned = cleaned.lastIndexOf('.') > cleaned.lastIndexOf(',')
          ? cleaned.replace(",", "")
          : cleaned.replace(".", "").replace(",", ".");
    } else if (cleaned.contains(",")) {
      cleaned = cleaned.replace(",", ".");
    }
    return new BigDecimal(cleaned);
  }

  static Optional<String> parseCurrency(String rawText) {
    Matcher isoMatcher = ISO_CODE_PATTERN.matcher(rawText);
    if (isoMatcher.find()) {
      return Optional.of(isoMatcher.group(1));
    }
    return CURRENCY_SYMBOLS.entrySet().stream()
        .filter(e -> rawText.contains(e.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();
  }

  public PaymentPage fillCardNumber(String cardNumber) {
    cardNumberInput.shouldBe(visible, LOAD_TIMEOUT).setValue(cardNumber);
    return this;
  }

  @Step("UI: Fill expiry date: {expiryDate}")
  public PaymentPage fillExpiry(String expiryDate) {
    expiryInput.shouldBe(visible).setValue(expiryDate);
    return this;
  }

  @Step("UI: Fill CVV")
  public PaymentPage fillCvv(String cvv) {
    cvvInput.shouldBe(visible).setValue(cvv);
    return this;
  }

  @Step("UI: Submit payment")
  public void submitPayment() {
    submitButton.shouldBe(visible).click();
  }

  @Step("UI: Complete payment with card details")
  public void pay(String cardNumber, String expiry, String cvv) {
    fillCardNumber(cardNumber);
    fillExpiry(expiry);
    fillCvv(cvv);
    submitPayment();
  }

  @Step("UI: Pay with test card {card}")
  public void pay(TestCard card) {
    fillCardNumber(card.number());
    fillExpiry(card.expiry());
    fillCvv(card.cvv());
    submitPayment();
  }

  /**
   * Simulates an impatient payer double-clicking Pay: fills the form once, then fires two
   * click events on Submit back-to-back in a single script. Two sequential Selenide
   * {@code .click()} calls don't reproduce a real double-click — the WebDriver round-trip
   * between them gives the page time to disable the button first, so the second click never
   * actually races anything. Dispatching both from JS in one call closes that gap. The
   * gateway (not this test) is responsible for making the second click a no-op — this only
   * proves the flow still settles exactly once, for the requested amount, instead of erroring
   * or visibly double-submitting.
   */
  @Step("UI: Pay with test card {card}, double-clicking submit")
  public void payWithDoubleClickSubmit(TestCard card) {
    fillCardNumber(card.number());
    fillExpiry(card.expiry());
    fillCvv(card.cvv());
    submitButton.shouldBe(visible);
    Selenide.executeJavaScript("arguments[0].click(); arguments[0].click();", submitButton);
  }
}
