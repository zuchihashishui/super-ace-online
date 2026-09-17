# V18 verification

- Backend: 56 tests passed, zero failures/errors; backend unchanged from verified V17.
- Packaged JAR Color Game browser suite passed: registration, guest restrictions, responsive layouts at 320/390/760/844/1440, chip stacks, hidden outcomes, payout calculation, retry/reload, history, audio, Filipino, two players and wallet isolation.
- Recovery browser suite passed: delayed polling snapshot cannot erase an accepted wager; lost accepted response retries without double debit; offline disables betting and reconnection recovers.
- Personal Dragon Tiger win presentation checks passed: profit shows net win, loss/refund do not celebrate, duplicate results do not replay, reduced-motion creates no particles.

Environment: Java 21, H2 with MySQL compatibility and Chromium against the actual packaged release JAR. Actual MySQL, native Windows and physical phones were not available. No claim of exhaustive testing or proof of the user's original unreported error.

Dragon Tiger packaged-JAR browser regression suite also passed: cumulative bets, settlement, reload/retry, wallet isolation, responsive UI, Filipino and sound.
