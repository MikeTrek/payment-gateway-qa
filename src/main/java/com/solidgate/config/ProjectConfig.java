package com.solidgate.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;
import org.aeonbits.owner.Config.Sources;

@LoadPolicy(LoadType.MERGE)
@Sources({
    "system:properties",
    "classpath:test.properties"
})
public interface ProjectConfig extends Config {

  @Key("solidgate.public.key")
  String publicKey();

  @Key("solidgate.secret.key")
  String secretKey();

  @Key("solidgate.payment.page.url")
  @DefaultValue("https://payment-page.solidgate.com/api/v1")
  String paymentPageBaseUrl();

  @Key("solidgate.pay.url")
  @DefaultValue("https://pay.solidgate.com/api/v1")
  String payBaseUrl();

  @Key("browser")
  @DefaultValue("chrome")
  String browser();

  @Key("browser.headless")
  @DefaultValue("false")
  boolean headless();

  @Key("browser.timeout")
  @DefaultValue("15000")
  long timeout();

  @Key("page.load.timeout")
  @DefaultValue("30")
  int pageLoadTimeoutSec();

  @Key("payment.result.timeout")
  @DefaultValue("60")
  int paymentResultTimeoutSec();

  @Key("status.poll.interval")
  @DefaultValue("3")
  int statusPollIntervalSec();

  @Key("status.poll.timeout")
  @DefaultValue("60")
  int statusPollTimeoutSec();
}
