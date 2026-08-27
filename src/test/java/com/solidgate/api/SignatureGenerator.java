package com.solidgate.api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Solidgate signature generator.
 * Algorithm: Base64( hex( HMAC-SHA512( publicKey + jsonBody + publicKey, secretKey ) ) )
 */
public final class SignatureGenerator {

  private SignatureGenerator() {
  }

  public static String generate(String publicKey, String jsonBody, String secretKey) {
    String data = publicKey + jsonBody + publicKey;
    try {
      Mac hmac = Mac.getInstance("HmacSHA512");
      hmac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
      byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      String hexHash = HexFormat.of().formatHex(hash);
      return Base64.getEncoder().encodeToString(hexHash.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate Solidgate signature", e);
    }
  }
}
