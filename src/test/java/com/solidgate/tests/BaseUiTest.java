package com.solidgate.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.solidgate.api.PaymentRequestFactory;
import com.solidgate.api.SolidgateApiClient;
import com.solidgate.config.ConfigProvider;
import com.solidgate.config.ProjectConfig;
import com.solidgate.model.InitPaymentRequest;
import com.solidgate.model.Money;
import com.solidgate.ui.pages.PaymentPage;
import com.solidgate.ui.pages.PaymentResultPage;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@ResourceLock("browser")
abstract class BaseUiTest {

  protected static final ProjectConfig CONFIG = ConfigProvider.config();
  protected SolidgateApiClient apiClient;
  protected PaymentPage paymentPage;
  protected PaymentResultPage resultPage;

  // Captured from the Payment Page at creation time — the UI elements are gone
  // by the time verifyOrderStatus() runs on the post-payment result page.
  private java.math.BigDecimal displayedAmountOnPage;
  private java.util.Optional<String> displayedCurrencyOnPage;

  // Same Chrome everywhere via Testcontainers, opt-in: -Duse.testcontainers=true.
  // Defaults to a locally installed Chrome so a plain `./mvnw test -Dgroups=ui` keeps
  // working on a machine without Docker.
  private static final boolean USE_TESTCONTAINERS =
      Boolean.getBoolean("use.testcontainers") || Boolean.getBoolean("selenide.remote");

  @BeforeAll
  static void setupBrowser() {
    if (USE_TESTCONTAINERS) {
      ContainerBrowserFactory.start();
    } else {
      Configuration.browser = CONFIG.browser();
      Configuration.headless = CONFIG.headless();
      Configuration.timeout = CONFIG.timeout();
      Configuration.browserCapabilities.setCapability("goog:chromeOptions",
          Map.of("args", List.of(
              "--no-sandbox",
              "--disable-dev-shm-usage",
              "--disable-gpu",
              "--remote-allow-origins=*"
          )));
    }
    SelenideLogger.addListener("allure", new AllureSelenide()
        .screenshots(true)
        .savePageSource(true));
  }

  @AfterAll
  static void teardownBrowser() {
    if (USE_TESTCONTAINERS) {
      ContainerBrowserFactory.stop();
    }
  }

  @BeforeEach
  void initClients() {
    apiClient = new SolidgateApiClient();
    paymentPage = new PaymentPage();
    resultPage = new PaymentResultPage();
  }

  @AfterEach
  void captureScreenshot() {
    if (WebDriverRunner.hasWebDriverStarted()) {
      byte[] png = ((TakesScreenshot) WebDriverRunner.getWebDriver())
          .getScreenshotAs(OutputType.BYTES);
      Allure.addAttachment("Final state",
          "image/png", new ByteArrayInputStream(png), ".png");
    }
  }

  @Step("Create payment page via API and open checkout URL")
  protected String createAndOpenPaymentPage(Money money) {
    String orderId = PaymentRequestFactory.generateOrderId();
    InitPaymentRequest request = PaymentRequestFactory.create(
        orderId, money.amountMinor(), money.currency(), "Test payment");

    Response response = apiClient.createPaymentPage(request);
    assertThat(response.statusCode()).isEqualTo(200);

    String checkoutUrl = response.jsonPath().getString("url");
    assertThat(checkoutUrl).isNotBlank();

    open(checkoutUrl);

    String displayedAmount = paymentPage.getDisplayedAmount();
    assertThat(displayedAmount)
        .as("Payment Page should display the correct amount")
        .isNotBlank();

    // Snapshot now — these DOM elements won't exist on the post-payment result page.
    displayedAmountOnPage = paymentPage.getDisplayedAmountValue();
    displayedCurrencyOnPage = paymentPage.getDisplayedCurrency();

    // Evidence, not just an assertion: record what was actually read, so a report reviewer
    // (human or automated) can see whether currency detection engaged or silently no-op'd.
    Allure.addAttachment("Parsed Payment Page price",
        "text/plain",
        "raw text: " + displayedAmount
            + "\nparsed amount: " + displayedAmountOnPage
            + "\nparsed currency: " + displayedCurrencyOnPage.orElse("<not detected>"));

    assertThat(displayedAmountOnPage)
        .as("Payment Page displayed amount should match the requested amount")
        .isEqualByComparingTo(money.majorUnits());
    displayedCurrencyOnPage.ifPresent(displayedCurrency ->
        assertThat(displayedCurrency)
            .as("Payment Page displayed currency should match the requested currency")
            .isEqualToIgnoringCase(money.currency()));

    return orderId;
  }

  @Step("Verify order status matches expected money")
  protected void verifyOrderStatus(String orderId, Money expected,
      String... acceptableStatuses) {
    // Poll only for status — it's the only eventually-consistent field
    Response finalResponse = Awaitility.await()
        .alias("Waiting for payment status")
        .pollInterval(Duration.ofSeconds(CONFIG.statusPollIntervalSec()))
        .atMost(Duration.ofSeconds(CONFIG.statusPollTimeoutSec()))
        .until(() -> {
          Response r = apiClient.getOrderStatus(orderId);
          assertThat(r.statusCode()).isEqualTo(200);
          return r;
        }, r -> {
          String s = r.jsonPath().getString("order.status");
          return s != null && java.util.Arrays.asList(acceptableStatuses).contains(s);
        });

    Money statusMoney = new Money(
        finalResponse.jsonPath().getInt("order.amount"),
        finalResponse.jsonPath().getString("order.currency"));
    assertThat(statusMoney)
        .as("/status amount+currency")
        .isEqualTo(expected);

    // Assignment requirement: amount/currency from /status must match what the
    // Payment Page actually displayed to the payer — not just what we requested.
    assertThat(displayedAmountOnPage)
        .as("/status amount should match what the Payment Page displayed")
        .isEqualByComparingTo(statusMoney.majorUnits());
    displayedCurrencyOnPage.ifPresent(displayedCurrency ->
        assertThat(displayedCurrency)
            .as("/status currency should match what the Payment Page displayed")
            .isEqualToIgnoringCase(statusMoney.currency()));
  }
}
