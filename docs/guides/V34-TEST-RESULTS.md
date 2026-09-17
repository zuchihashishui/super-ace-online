# V34 test results

- PASS color-history: controlled API fixtures; exact two-decimal frequencies, 20 results / two pages, three vertical colors per round, selection, refresh, empty states, Filipino, keyboard Escape and five viewport widths.
- PASS color-landscape against the final release JAR: emulated touch viewports 390×844, 844×390, 667×375 and 360×800; portrait rotation/native landscape, visible tap targets, modal taps, rejected fullscreen, real accepted wager and cleanup on exit.
- PASS color against the final release JAR: responsive layouts, guest/register, chip stacks, hidden outcomes, exact payouts, duplicate retry/reload, history, Filipino, audio, multiple players and Lobby/Club wallet isolation.

The first screenshot run exceeded its 12-second capture timeout under software WebGL; capture timeout was extended and screenshots were reviewed. A resize assertion initially ran before the browser applied the new viewport; the test now waits for the actual surface dimensions and orientation to settle. Final behavioral runs passed. Phone previews use an isolated test database; desktop History previews use deterministic test fixtures, not real player results.

Java 21 packaged release with isolated H2; no access to the user's MySQL. Chromium mobile emulation is not physical iPhone/Android certification. Native screen-lock success depends on device/browser permission; refusal was tested. No Java gameplay, database, payout or jackpot-funding changes.

Final run output:

```text
RUN color-landscape
Viewport 390 844
Viewport 844 390
Viewport 667 375
Viewport 360 800
PASS Phone landscape: portrait CSS rotation, native landscape, visible touch controls, dialogs, fullscreen rejection, real wager and cleanup on exit.
RUN color
PASS Color Game: responsive desktop/mobile, guest/register, stacked chips, hidden outcomes, exact payouts, retry/reload, history, Filipino, audio, two players and wallet isolation.
ALL SELECTED BROWSER CHECKS PASSED
```
