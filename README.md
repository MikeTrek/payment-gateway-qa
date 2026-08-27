# Payment Gateway QA

Automated API + UI tests for Solidgate payment processing.

## Start Here

The assignment asked for two tests. Both live here:

| # | Requirement | Test |
|---|---|---|
| 1 | Create Payment Page, pay via UI, verify the success page | [`PaymentUiTest.shouldCompleteVisaPaymentAndVerifyStatus`](src/test/java/com/solidgate/tests/PaymentUiTest.java) |
| 2 | `/status` amount/currency match what the Payment Page displayed, status successful | [`BaseUiTest.verifyOrderStatus`](src/test/java/com/solidgate/tests/BaseUiTest.java), called from test 1 |

Run just those with `./mvnw test -Dtest=PaymentUiTest#shouldCompleteVisaPaymentAndVerifyStatus`.

Everything else in this repo (webhook verification, 3D Secure, refund lifecycle, card-data-leakage checks, quarantine/retry infrastructure, Testcontainers, Allure/CI pipeline) is scope I added beyond the assignment — see [`core-assignment`](../../tree/core-assignment) for a branch containing only the two requested tests, if a lean read is more useful than the full one. The `## Test Coverage` and `## Design Decisions` sections below document the extra work for anyone who wants it.

## Quickstart

```bash
export SOLIDGATE_PUBLIC_KEY="api_pk_..."
export SOLIDGATE_SECRET_KEY="api_sk_..."

./mvnw clean test                                  # all tests
./mvnw test -DexcludedGroups=ui,quarantine         # API only
./mvnw test -Dgroups=ui -Dbrowser.headless=true    # UI only
./mvnw test -Dgroups=quarantine                    # quarantine lane
```

Requires only JDK 21 — Maven Wrapper (`mvnw`) is included.

## Assignment → Test Mapping

| Requirement | Test | File |
|---|---|---|
| **Test 1**: Create Payment Page → enter card → pay → verify success | `shouldCompleteVisaPaymentAndVerifyStatus` | PaymentUiTest |
| **Test 2**: Check order status — amount/currency match Payment Page | `verifyOrderStatus` (via Money VO assertion) | BaseUiTest |
| Signature: `Base64(hex(HMAC-SHA512(pub+body+pub, secret)))` | `SignatureGenerator` + `SignatureAndAuthTest` | api/ |

## Test Coverage

| Class | Tests | Type | What it proves |
|---|---|---|---|
| **PaymentUiTest** | 5 | UI+API | Visa/MC E2E, auth_failed, general decline, 3DS challenge flow |
| **OrderLifecycleTest** | 2 | UI+API | Pay → settle → refund → verify refunded_amount on /status; double-click submit doesn't double-charge |
| **CardDataLeakageTest** | 4 | UI | PAN not in DOM/URL/localStorage/sessionStorage; HTTPS enforced |
| **OrderStatusApiTest** | 8 | API | /status exact values, /init, zero-decimal JPY, idempotency, JSON schema validation |
| **SignatureAndAuthTest** | 5 | Unit+API | HMAC determinism, Base64 format, invalid sig, wrong merchant |
| **WebhookSignatureTest** | 5 | Unit | Verify/reject webhooks, tampered body, wrong secret, missing signature fails closed, algorithm parity |
| **FlakyRetryTest** | 4 | Unit | Retry policy: retries WebDriver/timeout flakiness, never retries a real assertion failure |
| **Total** | **33** | | |

## Architecture

```
src/
├── main/java/com/solidgate/
│   ├── config/          # Owner-based typed config (env vars → dotted keys)
│   └── model/           # Money VO, TestCard enum, request DTOs
└── test/java/com/solidgate/
    ├── api/             # API client, signature gen, webhook verifier
    ├── tests/           # Test classes + BaseUiTest + ContainerBrowserFactory
    └── ui/pages/        # Page Objects (Payment, Result, 3DS Challenge)
```

## Tech Stack

| Layer | Tool | Why |
|---|---|---|
| Language | Java 21 | Assignment requirement |
| UI | Selenide 7.7 | Fluent API, auto-waits, Allure integration |
| API | Rest Assured 5.5 | JSON schema validation, Allure filter |
| Assertions | AssertJ 3.26 | Readable fluent assertions |
| Polling | Awaitility 4.2 | Polls /status for eventual consistency |
| Config | Owner 1.0 | Type-safe, env-var-injected properties |
| Containers | Testcontainers 1.20 | Identical Chrome in CI, failure video recording |
| Reporting | Allure 2.29 | Screenshots, API logs, GitHub Pages deploy |
| Linting | Checkstyle (Google) | severity=error, CI-enforced |
| CI | GitHub Actions | API → UI → Quarantine → Allure Pages |

## Design Decisions

**Money value object** — Amount (minor units) and currency travel together. `assertThat(statusMoney).isEqualTo(pageMoney)` eliminates the bug class where they're compared separately. JPY (zero-decimal) handling is built in.

**JSON schema validation** — `matchesJsonSchemaInClasspath` on /status and /init responses catches silent field renames that would otherwise surface as null dereferences.

**Quarantine lane** — Flaky tests (3DS sandbox timing) are tagged `@Tag("quarantine")` and run in a separate CI step with `continue-on-error: true`. They report to Allure but don't gate the build. This inverts "I deleted what I couldn't fix" into "I classified my instability." `FlakyRetry.run(...)` backs this up mechanically, not just organizationally: each quarantined test body is wrapped in it, retrying up to 3 times on `WebDriverException`/Awaitility timeout (a dropped session, a sandbox hiccup) but rethrowing immediately on any `AssertionError` — a real bug can never be retried into a false pass. (An earlier version implemented this as a JUnit `InvocationInterceptor` instead of a plain wrapper — it compiled and passed its own unit test, but broke in a real CI run: JUnit Jupiter rejects an interceptor calling `proceed()` more than once once another interceptor, like the built-in `TimeoutExtension`, is in the chain. The plain-method version sidesteps that class of engine-internals risk entirely.) `FlakyRetryTest` proves both halves of the retry policy without a browser.

**Webhook signature verifier** — Implements the receiving side of Solidgate v1 webhook verification (HMAC-SHA512, constant-time comparison). Unit tests prove verify/reject/tamper detection without needing a public URL.

**Testcontainers** — `ContainerBrowserFactory` gives identical Chrome across environments plus automatic VNC failure recording. Opt-in via `-Duse.testcontainers=true` (default stays local Chrome, so a cold clone without Docker still works); CI runs a dedicated non-gating smoke step through this path so the option is proven to actually work, not just present in the source tree.

**Parallel execution** — `junit-platform.properties` runs test classes concurrently (parallelism=4). API tests are thread-safe; UI tests inherit from `BaseUiTest` with per-class browser state.

## CI Pipeline

```
Checkstyle → API tests → UI tests → Quarantine (non-gating) → Allure report → GitHub Pages deploy
```

Live report: `https://<username>.github.io/payment-gateway-qa/`

## What I Chose Not to Test — and Why

| Excluded | Reason |
|---|---|
| **Webhook E2E delivery** | Requires a publicly reachable URL (ngrok/tunnel). Unit-tested the verification algorithm instead — documenting the gap is better than importing a fragile dependency. |
| **Load / performance testing** | Out of scope for a functional test suite. Would use Gatling or k6 separately. |
| **Card brand visual rendering** | Brand logos are Solidgate's Payment Page concern, not the merchant integration's. |
| **Error card amount variants** | 15 amount→error combinations all post the same /init body and never observe the error through the API. One representative test (AMT_3_01) proves the pattern; the rest would inflate count without adding coverage. |
| **Multi-currency E2E** | Tested JPY (zero-decimal) at the API level. A full E2E for every currency would need sandbox support confirmation per currency. |
| **Network-level PAN capture / HSTS check via CDP** | Tried it (Selenium's `Network.enable` + `requestWillBeSent`/`responseReceived`). It broke in CI with `DevToolsException: no-op implementation of the CDP` — Selenium 4.29's bundled `selenium-devtools-v131/132/133` don't cover whatever Chrome version `browser-actions/setup-chrome@v1`'s `stable` channel installs today, and that gap only grows as Chrome keeps shipping. Fixing it properly means bumping Selenium/Selenide across the whole suite for one bonus check — too much blast radius for already-passing, already-verified tests. DOM/URL/localStorage/sessionStorage checks plus HTTPS-scheme verification cover the leakage surface that's actually stable to test; a broken "ambitious" test is worse than not having it. |

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `SOLIDGATE_PUBLIC_KEY` | Yes | Merchant public API key (`api_pk_...`) |
| `SOLIDGATE_SECRET_KEY` | Yes | Merchant secret key for HMAC signing (`api_sk_...`) |

Missing credentials fail fast with a clear `IllegalStateException` message.
