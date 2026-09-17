# V30 — Color Game result belongs on the top face

The previous cube placed the authoritative outcome on cf-0 (front), while cf-4 (top) could be another color. A pink outcome could therefore show a red star on top. This was an ambiguous UI mapping, not a RED/PINK payout swap.

The outcome is now assigned to cf-4, the upper face. All six colors remain distinct faces; the top result has a bright border and side faces are shaded. The final cube angle and animation endpoint both use rotateX(-32deg) rotateY(24deg), exposing the upper face more clearly. The localized color label remains below each die. RED always uses a star and PINK a heart.

Client version: 36-color-face. No Java, payout or database changes. Stop the old server, run the new release and reload with Ctrl+F5. Keep the existing database.

Verification: packaged release tested in Chromium with Java 21 / isolated H2. All six colors were checked across three dice for top-face class, symbol, localized label and six unique faces. A forced GREEN/RED/RED round had matching top faces and returned 30 for RED/YELLOW/WHITE stakes of 10 each. The three-color limit, repeat top-ups, new-round reset and five responsive widths passed. Screenshot visually reviewed at mobile width. No backend code changed or backend unit tests rerun. No physical phone or user MySQL testing.

The full Color Game regression suite also passed (guest/register, stacked chips, retry/reload, hidden outcomes, history, Filipino, audio, multiplayer and wallet isolation). Its mode-switch step now retries after the polling/busy guards clear; this changes test synchronization only.
