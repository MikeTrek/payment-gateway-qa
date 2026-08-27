package com.solidgate.api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Verifies incoming Solidgate webhook v1 signatures.
 * Algorithm mirrors SignatureGenerator: Base64(hex(HMAC-SHA512(whPublicKey + body + whPublicKey, whSecretKey))).
 */
public final class WebhookSignatureVerifier {

  private WebhookSignatureVerifier() {
  }

  /**
   * Returns true when the supplied signature matches the expected HMAC for the given body.
   */
  public static boolean verify(String webhookPublicKey, String body,
      String webhookSecretKey, String signature) {
    String expected = computeSignature(webhookPublicKey, body, webhookSecretKey);
    return constantTimeEquals(expected, signature);
  }

  static String computeSignature(String publicKey, String body, String secretKey) {
    String data = publicKey + body + publicKey;
    try {
      Mac hmac = Mac.getInstance("HmacSHA512");
      hmac.init(new SecretKeySpec(
          secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
      byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
      String hexHash = HexFormat.of().formatHex(hash);
      return Base64.getEncoder().encodeToString(hexHash.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to compute webhook signature", e);
    }
  }

  // Constant-time comparison prevents timing side-channels; fails closed on a missing signature
  // instead of throwing, so a null/absent header is rejected rather than crashing the webhook
  // handler.
  private static boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null || a.length() != b.length()) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < a.length(); i++) {
      result |= a.charAt(i) ^ b.charAt(i);
    }
    return result == 0;
  }
}
