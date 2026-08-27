package com.solidgate.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.utility.DockerImageName;

import java.io.File;

/**
 * Optional Testcontainers-backed base for UI tests.
 * Activate via system property: {@code -Dselenide.remote} or {@code -Duse.testcontainers=true}.
 * <p>
 * Provides identical Chrome everywhere (local, CI, macOS, Linux) plus
 * automatic VNC failure recording that attaches to Allure.
 */
public final class ContainerBrowserFactory {

  private static final BrowserWebDriverContainer<?> CHROME =
      new BrowserWebDriverContainer<>(
          DockerImageName.parse("selenium/standalone-chrome:131.0"))
          .withRecordingMode(VncRecordingMode.RECORD_FAILING,
              new File("target/videos"));

  private ContainerBrowserFactory() {
  }

  /**
   * Start the container and configure Selenide to use its RemoteWebDriver.
   * Call from {@code @BeforeAll} instead of configuring a local browser.
   */
  public static void start() {
    if (!CHROME.isRunning()) {
      CHROME.start();
    }
    RemoteWebDriver driver = CHROME.getWebDriver();
    Configuration.browser = "chrome";
    WebDriverRunner.setWebDriver(driver);
  }

  /**
   * Stop the container and clean up. Call from {@code @AfterAll}.
   */
  public static void stop() {
    if (CHROME.isRunning()) {
      CHROME.stop();
    }
  }
}
