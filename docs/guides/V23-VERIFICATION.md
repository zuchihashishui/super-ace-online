# V23 verification

Full backend suite: 75 tests, zero failures/errors. Covers existing auth, roles, wallets, transfers, reports and games, plus new engines, payout rounding, request replay, concurrency and migrations through V18.

Packaged-JAR browser suites passed for Crash, Plinko, Lucky Wheel, Slots, Mines, Color Game and Dragon Tiger. Checks include guest/register, desktop/mobile/landscape layouts, sound/language, wallet separation, accepted-response loss and recovery, payout/history consistency, hidden future outcomes and mine positions. Mines additionally checks that mobile cashout stays in the viewport while the grid is visible.

ZIP packaging validates CRC, extraction, JAR checksum and exact client/JAR parity.

Environment: Java 21, Chromium and H2 in MySQL compatibility mode. Actual MySQL, native Windows and physical phones were not available. This is implementation verification, not a certification or external fairness audit.

Not implemented: Sakla, Lucky 9 and Bingo. Their provided menu banners do not establish full game variants, turn rules or paytables; gameplay / How to play references are needed before implementing them to match the requested app.
