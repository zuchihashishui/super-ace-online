# V26 — complete game collection and verification

Release bundle V26, database schema V19. All eleven requested games are playable:
Super Ace, Dragon Tiger, Color Game, Crash, Plinko, Lucky Wheel, Seven Slots, Mines, Sakla, Lucky 9 and Bingo.
Sakla, Lucky 9 and Bingo use the explicit independent variants documented in their in-game How to play screens. They are not represented as exact copies of another operator's backend.

## Changes

- Color Game: three true six-face CSS cubes drop from above, rotate, bounce independently and settle before the result caption. The server's result remains authoritative. Inactive tabs finish the visual sequence; reduced motion skips tumbling. Animation cancellation respects player, wallet and round ownership.
- Positive net wins: view-scoped gold announcement, counted amount, rays and bounded coin bursts, with volume/mute and reduced-motion support. Refunded stakes and net losses never trigger this celebration. Dragon Tiger retains its dedicated win sequence; Super Ace retains its cascade and big-win announcements.
- Sakla: choose one of 20 pairs, reveal the shuffled 40-card deal through the first complete pair, settle once and retain the receipt.
- Lucky 9: two cards, optional hit/stand, private banker/deck, natural outcomes, ties, persisted unfinished hands and idempotent decisions. Mobile decision buttons remain visible beside the card table.
- Bingo: 75-ball ticket, 30 calls, automatic marking, 12 line patterns, one fixed gross payout even for multiple lines, and history.
- Root Java package `philip.emerald.ace`; each game has its own package. Shared code is in `Utils`.
- MySQL schema V19 and all matching upgrade files generated from immutable Flyway migrations.

## Completed checks

92 backend tests passed under Java 21. They cover game rules/paytables, all 4096 Plinko paths, all 1000 Slots combinations, Bingo line inclusion–exclusion, card/deck uniqueness, hidden results, natural/hit/stand/tie branches, wallet and report isolation, duplicate/concurrent decisions, authentication/hierarchy and migrations preserving existing records.

Actual packaged-JAR browser checks passed for all eleven games, plus the connection-recovery/effects suite (12 suites total). Covered behaviors include:

- Guest viewing, UI registration with repeated password, login/logout and mobile drawer.
- 320, 390, 760, 844 and 1440 pixel layouts, including landscape and touch targets.
- Accepted requests whose responses are lost; retry and reload charge/pay exactly once.
- Active Lucky 9/Mines restoration, hidden banker/mine state and persisted outcomes.
- Shared timed Dragon Tiger/Color Game tables, cumulative chip stacks and other-player totals.
- Crash server cashout while the browser is offline.
- Lobby/Club separation, insufficient balances and twenty-receipt histories.
- English/Filipino, audio context/mute, reduced motion, dice lifecycle and positive-net-only celebrations.
- Bingo completes to the same receipt when the page becomes hidden during animation.

Runtime: Java 21.0.8, Spring Boot 3.5.16, H2 2.3.232 in MySQL mode, Chromium/Playwright. Tests use disposable databases, not the user's MySQL. Real MySQL 8 migration execution, Windows batch execution/Explorer extraction, physical phones and audible speaker quality were not tested here. Archive CRC and complete extraction are checked locally.

## Run the checks

With a Java 21 JDK and Maven: `mvn -f server/pom.xml clean verify`.

For packaged browser tests, install Node.js, Playwright/Chromium and the H2 test JAR, then set `ACE_JAVA`, `ACE_H2_JAR`, `ACE_PLAYWRIGHT` and `ACE_CHROMIUM` as needed. Run:

```sh
python tools/run_browser_checks.py
# Or a subset:
python tools/run_browser_checks.py superace lucky9 bingo
```

The runner creates isolated H2 databases and starts the release JAR itself. It never selects your MySQL database.

## Upgrade

Stop the old server and preserve your existing application.properties settings. Extract the complete V26 folder and start it with Java 21. The release JAR retains its launcher-compatible filename `release/super-ace-online-13.0.0.jar`; the bundle version is V26. Flyway applies missing migrations through V19 on startup.

Do not import `database/super_ace.sql` over an existing database. That file is for fresh installs only. For a manual upgrade, use the matching `upgrade_vN_to_v19.sql` once with the server stopped; do not also run it after Flyway has migrated.

V24 and V25 ZIPs are earlier cumulative milestones. V26 contains both and Bingo; only the V26 ZIP is needed for the full collection.
