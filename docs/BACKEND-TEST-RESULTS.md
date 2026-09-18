# Backend verification — V9, 2026-09-16

31 tests passed, 0 failures, 0 errors, 0 skipped:

| Suite | Tests |
| --- | ---: |
| GameEngineTest | 5 |
| UpgradeTest | 4 |
| ServerTest | 21 |
| MigrationV9Test | 1 |

The main suite passed 30 tests in one Maven package run. The additional
existing-data migration test passed in a second targeted package run.
Runtime: Temurin Java 21.0.8. Maven 3.9.9 used Eclipse compiler 3.36.0 through
a temporary compiler configuration because the downloaded runtime lacks javac.
All backend classes were compiled to Java 21. The normal project pom remains
unchanged and builds with a full JDK 21 using `mvn package`.

Coverage includes Creator-only RTP GET/PUT authorization, decimal 97.55%
storage, stale revision rejection, invalid ranges/precision, current paid-round
profiles and Free Spin profile pinning. All four roles pass manual spin and
autoplay checks in both modes with isolated wallets and correct attribution.
V8 unassigned Player removal/join approval tests also passed.

MigrationV9Test upgraded a populated V7 H2 database through V8/V9, preserving
password hashes, wallets, parent links and historical ledger attribution;
verified both default RTP settings equal 97% and new unassigned rounds work.
A standalone Java check also passed decimal scaling, rounded payouts, legacy
profile compatibility and rejection of invalid profiles.

The actual release JAR passed tools/simple_auth_check.py, including its live
HTTP and jsdom UI checks. These cover registration/login/logout/token revocation,
wallet isolation, transfers, chip notifications, Creator RTP editing to 97.55%,
hidden RTP for Player/Agent/Super Agent, Game menus and retained management
menus. Client JavaScript syntax and git diff checks passed.

Release includes current client, compiled backend and V1–V9 migrations.
Checksum: release/SHA256SUMS.txt. Tests used disposable H2 in MySQL mode,
not the user's local MySQL. Native MySQL migration, real-device visual testing,
load tests and long-run RTP simulation were not performed. jsdom verifies DOM
behavior but does not validate actual mobile rendering. No GitHub push or
remote Actions run is claimed.
