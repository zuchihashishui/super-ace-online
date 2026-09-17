# V24 — Sakla and presentation refresh

Nine playable games: Super Ace, Dragon Tiger, Color Game, Crash, Plinko, Lucky Wheel, Slots, Mines and Sakla.

Sakla uses a shuffled 40-card Spanish deck, 20 fixed pairs, and stops at the first completed pair. Select one pair per ticket. A correct selection returns 19 times the stake, including the stake. All pairs have equal probability; theoretical gross return is 95%. This is an independent single-player implementation, with rules visible before betting.

Rule reference: https://saklanation.weebly.com/sakla-rules.html

Color Game now has three six-face CSS cubes which fall, tumble, bounce and settle before the result caption. Positive net wins have a gold announcement, count-up and bounded coin burst. Dragon Tiger retains its dedicated victory sequence. Sound/mute and reduced-motion preferences are respected. Presentation never chooses or changes a financial outcome.

Java root: `philip.emerald.ace`. Game engines/services are in individual game packages. Shared wallet, security, reporting, hierarchy and dispatch code are under `Utils`. Stored receipts and old migration checksums remain compatible. No database migration is required for Sakla.

Lucky 9 and Bingo follow in the next cumulative releases.

Verification: 75 existing backend tests passed after package migration, then 46 targeted backend tests including Sakla passed. Actual release-JAR browser checks passed at 320/390/760/844/1440px, including guest registration, selected-pair payout, accepted-response loss and reload recovery, Lobby/Club separation, Filipino, positive-net-only celebration and the three-cube animation lifecycle. Runtime: Java 21, disposable H2 in MySQL mode and Chromium. A live MySQL server, Windows Explorer and physical mobile devices were not available for testing.
