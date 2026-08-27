package com.solidgate.ui.pages;

import com.codeborne.selenide.SelenideElement;
import com.solidgate.config.ConfigProvider;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.switchTo;

/**
 * Handles the 3D Secure challenge page in Solidgate sandbox.
 * The sandbox simulator shows "Confirm Payment" / "Cancel" buttons (no code input).
 */
public class ThreeDsChallengePage {

  private static final Duration CHALLENGE_TIMEOUT =
      Duration.ofSeconds(ConfigProvider.config().paymentResultTimeoutSec());

  private final SelenideElement challengeIframe = $("iframe[name*='challenge'], iframe[src*='3ds']");
  private final SelenideElement confirmButton =
      $x("//button[contains(text(),'Confirm')]");

  @Step("UI: Switch into 3DS challenge iframe")
  public ThreeDsChallengePage switchToChallengeFrame() {
    challengeIframe.shouldBe(visible, CHALLENGE_TIMEOUT);
    switchTo().frame(challengeIframe);
    return this;
  }

  @Step("UI: Confirm 3DS challenge")
  public void completeChallenge() {
    confirmButton.shouldBe(visible, CHALLENGE_TIMEOUT).click();
    switchTo().defaultContent();
  }
}
