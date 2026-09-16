# Backend verification — 2026-09-15

Runtime: Eclipse Temurin Java 21.0.12.1, Maven 3.9.9.

`cd server && mvn -B package`: BUILD SUCCESS.

| Suite | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| GameEngineTest | 5 | 0 | 0 | 0 |
| UpgradeTest | 4 | 0 | 0 | 0 |
| ServerTest | 18 | 0 | 0 | 0 |
| Total | 27 | 0 | 0 | 0 |

The initial run found one stale weekly-boundary assertion using UTC+7.
The test now explicitly uses Asia/Manila: Monday 2026-08-03 00:00 is
Sunday 2026-08-02 16:00 UTC. Both Spring test fixtures explicitly configure
their test Creator username, so they no longer depend on deployment defaults.
Production timezone and game probabilities were not changed in this update.

Coverage includes game payout rules, free spins and autoplay accounting,
concurrent requests, wallet isolation, transfer authorization and idempotency,
membership approval/rejection/removal, scoped Club balances, Agent gameplay,
weekly report boundaries and exclusion of Agent personal losses from commission.

The release JAR was rebuilt by Maven with the current server, client and V1–V7
migrations. Its checksum is in release/SHA256SUMS.txt.
The actual release JAR also passed tools/simple_auth_check.py with ACE_SKIP_DOM=1:
registration validation, duplicate usernames, generated IDs, default club and
wallets, login, wrong passwords, refresh, logout and token revocation, isolated
spins/retries/reports, hierarchical transfers, insufficient-balance and scope
checks, chip notifications and independent autoplay endpoints.

To repeat the packaged backend check, set ACE_JAVA to a Java 21 executable,
ACE_H2 to an H2 driver JAR, and run:

```sh
ACE_SKIP_DOM=1 python3 tools/simple_auth_check.py
```

Limitations: tests use disposable H2 databases in MySQL compatibility mode.
Fresh V1–V7 migrations succeeded; this does not validate native MySQL or an
upgrade against an existing production database. No connection was made to
the user's local MySQL. Browser/DOM, visual, load and long-run RTP testing
were not performed in this backend run. The local GitHub Actions workflow
now runs all backend tests; no remote workflow execution is claimed.
