package com.solidgate.config;

import org.aeonbits.owner.ConfigFactory;

import java.util.HashMap;
import java.util.Map;

public final class ConfigProvider {

  private static final ProjectConfig CONFIG = createConfig();

  private ConfigProvider() {
  }

  public static ProjectConfig config() {
    return CONFIG;
  }

  private static ProjectConfig createConfig() {
    Map<String, String> envMapped = new HashMap<>();
    // Map UPPER_SNAKE env vars to dotted keys for Owner resolution
    System.getenv().forEach((key, value) ->
        envMapped.put(key.toLowerCase().replace('_', '.'), value));
    ProjectConfig cfg =
        ConfigFactory.create(ProjectConfig.class, System.getProperties(), envMapped);
    validate(cfg);
    return cfg;
  }

  private static void validate(ProjectConfig cfg) {
    if (cfg.publicKey() == null || cfg.publicKey().isBlank()) {
      throw new IllegalStateException(
          "SOLIDGATE_PUBLIC_KEY is not set. Export it as env var or pass via -D.");
    }
    if (cfg.secretKey() == null || cfg.secretKey().isBlank()) {
      throw new IllegalStateException(
          "SOLIDGATE_SECRET_KEY is not set. Export it as env var or pass via -D.");
    }
  }
}
