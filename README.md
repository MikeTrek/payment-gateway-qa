# Payment Gateway QA — core assignment

This branch contains exactly what the assignment asked for, nothing more. For the full
version with additional coverage (webhooks, 3D Secure, refund lifecycle, security checks,
CI/reporting), see the [`main`](../../tree/main) branch.

## Quickstart

```bash
export SOLIDGATE_PUBLIC_KEY="api_pk_..."
export SOLIDGATE_SECRET_KEY="api_sk_..."

./mvnw clean test
```

Requires only JDK 21 — Maven Wrapper (`mvnw`) is included.

## Assignment → Test Mapping

| Requirement | Where |
|---|---|
| Create Payment Page for an order, pay it via UI, verify the success page | [`PaymentUiTest.shouldCompleteVisaPaymentAndVerifyStatus`](src/test/java/com/solidgate/tests/PaymentUiTest.java) |
| Check `/status`: amount/currency match what the Payment Page displayed, status successful | [`BaseUiTest.verifyOrderStatus`](src/test/java/com/solidgate/tests/BaseUiTest.java), called from the same test |
| Signature: `Base64(hex(HMAC-SHA512(pub+body+pub, secret)))` | [`SignatureGenerator`](src/test/java/com/solidgate/api/SignatureGenerator.java) |

The test creates and pays its own fresh order — `order_example` is only mentioned in the
assignment for manual `/status` debugging and isn't used in the test script, as instructed.

## Architecture

```
src/
├── main/java/com/solidgate/
│   ├── config/    # env vars -> typed config (Owner)
│   └── model/     # Money, TestCard, request DTOs
└── test/java/com/solidgate/
    ├── api/       # API client + signature generation
    ├── tests/     # PaymentUiTest, BaseUiTest
    └── ui/pages/  # Payment Page / result page objects
```

## Tech Stack

Java 21, Selenide (Selenium), Rest Assured, AssertJ, Awaitility (for polling `/status`),
Owner (config), Checkstyle.

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `SOLIDGATE_PUBLIC_KEY` | Yes | Merchant public API key (`api_pk_...`) |
| `SOLIDGATE_SECRET_KEY` | Yes | Merchant secret key for HMAC signing (`api_sk_...`) |

Missing credentials fail fast with a clear `IllegalStateException` message.
