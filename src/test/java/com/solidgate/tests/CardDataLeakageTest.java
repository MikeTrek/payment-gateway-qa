package com.solidgate.tests;

import com.codeborne.selenide.WebDriverRunner;
import com.solidgate.model.Money;
import com.solidgate.model.TestCard;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Security")
@Feature("Card Data Leakage")
@Tag("ui")
@Tag("quarantine")
class CardDataLeakageTest extends BaseUiTest {

  private static final Money FIVE_USD = new Money(500, "USD");

  @Test
  @Severity(SeverityLevel.BLOCKER)
  @DisplayName("Security: PAN never appears in DOM after payment submission")
  @Description("After card entry and submission, the full PAN must not exist in the page source")
  void shouldNotLeakPanInDom() {
    FlakyRetry.run("shouldNotLeakPanInDom", () -> {
      createAndOpenPaymentPage(FIVE_USD);

      paymentPage.pay(TestCard.VISA_SUCCESS);
      resultPage.verifyPaymentSuccess();

      String pageSource = WebDriverRunner.getWebDriver().getPageSource();
      assertThat(pageSource)
          .as("Full PAN must not appear in page source after submission")
          .doesNotContain(TestCard.VISA_SUCCESS.number());
    });
  }

  @Test
  @Severity(SeverityLevel.BLOCKER)
  @DisplayName("Security: PAN not stored in localStorage or sessionStorage")
  @Description("Card data must not persist in browser storage after payment")
  void shouldNotStorePanInBrowserStorage() {
    FlakyRetry.run("shouldNotStorePanInBrowserStorage", () -> {
      createAndOpenPaymentPage(FIVE_USD);

      paymentPage.pay(TestCard.VISA_SUCCESS);
      resultPage.verifyPaymentSuccess();

      WebDriver driver = WebDriverRunner.getWebDriver();
      String localStorage = (String) ((JavascriptExecutor) driver)
          .executeScript(
              "return JSON.stringify(Object.keys(localStorage)"
                  + ".reduce((a,k) => { a[k]=localStorage[k]; return a; }, {}))");
      String sessionStorage = (String) ((JavascriptExecutor) driver)
          .executeScript(
              "return JSON.stringify(Object.keys(sessionStorage)"
                  + ".reduce((a,k) => { a[k]=sessionStorage[k]; return a; }, {}))");

      assertThat(localStorage)
          .as("PAN must not appear in localStorage")
          .doesNotContain(TestCard.VISA_SUCCESS.number());
      assertThat(sessionStorage)
          .as("PAN must not appear in sessionStorage")
          .doesNotContain(TestCard.VISA_SUCCESS.number());
    });
  }

  @Test
  @Severity(SeverityLevel.BLOCKER)
  @DisplayName("Security: Payment page served over HTTPS")
  @Description("The checkout URL must use HTTPS — plain HTTP would expose card data in transit")
  void shouldUseHttps() {
    FlakyRetry.run("shouldUseHttps", () -> {
      createAndOpenPaymentPage(FIVE_USD);

      String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
      assertThat(currentUrl)
          .as("Payment page must be served over HTTPS")
          .startsWith("https://");
    });
  }

  @Test
  @Severity(SeverityLevel.CRITICAL)
  @DisplayName("Security: PAN not in current page URL")
  @Description("Card number must never appear as a URL parameter or path segment")
  void shouldNotLeakPanInUrl() {
    FlakyRetry.run("shouldNotLeakPanInUrl", () -> {
      createAndOpenPaymentPage(FIVE_USD);

      paymentPage.pay(TestCard.VISA_SUCCESS);
      resultPage.verifyPaymentSuccess();

      String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
      assertThat(currentUrl)
          .as("PAN must not appear in the URL")
          .doesNotContain(TestCard.VISA_SUCCESS.number());
    });
  }
}
