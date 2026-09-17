# V34 — Reference History and phone landscape

History now uses the supplied wooden-cabinet layout: six colored cards, percentages with two decimals, ten numbered columns with three colors each, and a gold border around the newest result. Two pages retain the latest 20 rounds. Frequencies are calculated from these actual results (up to 60 dice), not copied from the reference screenshot. Tap a column for the existing round time, stake and net. Empty history shows dashes. Refresh, keyboard Escape, English and Filipino remain supported.

Color Game uses a dedicated landscape surface on touch phones. Portrait devices display the game rotated 90 degrees; turning the device to landscape removes the CSS rotation. This includes top-layer dialogs. The Full screen button requests native fullscreen and landscape locking if supported; rejection keeps CSS rotation usable. The Games button returns to Super Ace and releases only the fullscreen/orientation acquired by Color Game. Other games retain their existing responsive layout. Browser chrome and device orientation permissions cannot be controlled universally.

The Jackpot remains inactive. See [the researched proposal](COLOR-JACKPOT-PROPOSAL.md) for the supplied rules, unresolved details and a concrete alternative adapted to current Gold denominations. No jackpot funding, payout or wallet changes are part of this release.

No SQL change; schema V19. Stop the previous server, use start.bat from the new package, preserve database/configuration and reload with Ctrl+F5. Client cache tag 40-history.

Validation: actual Java 21 release JAR with isolated H2, browser history fixture tests and the existing Color Game regression; added touch-phone tests for portrait/native landscape, visible controls, modal taps, rejected fullscreen, a real accepted wager and leaving the game. Tests do not connect to the user's MySQL or certify every physical iOS/Android browser. See the final test log included with the release for pass/fail results.

Browser references: https://developer.mozilla.org/en-US/docs/Web/API/ScreenOrientation/lock and https://developer.mozilla.org/en-US/docs/Web/API/Element/requestFullscreen .
