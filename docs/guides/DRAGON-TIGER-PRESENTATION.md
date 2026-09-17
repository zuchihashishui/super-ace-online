# Dragon Tiger presentation update

This release changes the client presentation, not betting rules, wallet accounting, timing or payouts. It requires no new database migration (schema stays V13).

## Table and mobile controls
- Emerald felt with red Dragon, blue Tiger and gold Tie betting areas; original SVG emblems and CSS card/chip artwork.
- Compact phone layout, sticky chip console, 44px minimum chip targets, visible round ID and connection freshness indicator.
- Chip flight and confirmation feedback, 10-second countdown with final-three-second warning, opening/closing banners, card deal/flip effects and winner highlighting.
- Full-screen control appears only when the browser supports it.
- Motion follows the system reduced-motion preference. Decorative flights are capped at eight and cleared on tab hiding.

## Audio
- A dedicated sound button opens mute, volume and Test sound controls directly from Dragon Tiger.
- Preferences persist in local storage and share the application's existing audio controls.
- Web Audio generates short chip clicks, card movement, countdown, opening/closing and win sounds. There are no third-party audio assets or background downloads.
- Audio must be unlocked by a user gesture. Test sound or tapping a chip does this; autoplay sound on first page visit is not promised.
- Hidden tabs do not emit Dragon Tiger sound cues. This does not pause server betting or settlement.

## History
- Latest 20 recorded completed games, real Dragon/Tiger/Tie result counts, per-game stakes/payouts and net.
- Tap a result tile, or focus it and press Enter/Space, to inspect the actual four cards and scores from that completed game.
- The result grid describes history, not future probabilities. No fake audience counts, simulated other-player bets or live-dealer video.

## Rules comparison
The existing user-requested variant uses two cards per side, summed modulo ten. It is not standard one-card Dragon Tiger. Evolution's First Person Dragon Tiger describes one card on Dragon and one on Tiger, with the higher card winning:
https://games.evolution.com/first-person/first-person-dragon-tiger/
The present release preserves the previously selected two-card rules and paytable; it does not claim exact equivalence to a particular Philippine provider or live casino.

## Verification
The packaged JAR was tested with Chromium at 320, 390, 760 and 1440px, including chip betting, automatic reveal, the 20-game history, card inspection, AudioContext activation, volume/mute and persistence after reload. Existing retry recovery, wallet separation and Filipino checks remain in the browser suite.
The backend is unchanged from the preceding 44-test build. Tests use disposable H2; native Windows, physical phone audio output and the user's MySQL were not exercised here.
