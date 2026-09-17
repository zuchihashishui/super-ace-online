# V35 validation

## Backend — passed

Java 21, Spring Boot, isolated H2 2.3.232 in MySQL compatibility mode.

`ServerTest, ColorJackpotMathTest, ColorJackpotServiceTest, MigrationV20Test`: **56 tests, 0 failures, 0 errors, 0 skipped**.

- All 10,000 random-selection positions match the published tier weights.
- Exact-cent contribution carry, prize flooring, proportional distribution and large-number arithmetic.
- Concurrent settlement credits one award per round/player, with exact pool conservation.
- Accepted wagers debit only their normal stake; retry does not enroll a second jackpot bet.
- Jackpot credits survive subsequent base-game settlement, including a losing base-color bet.
- Empty rounds, other dice colors and the disabled Club pool do not pay a jackpot.
- Creator funding is authorized, scoped and idempotent; other roles are denied.
- Award receipts belong to their recipient; repeated acknowledgment does not credit anything.
- Upgrade from V19 preserves account wallets; V20 seed executes once.
- Existing account/JWT/login/logout, hierarchy, transfer, report, game/wallet and duplicate-request regression checks remain passing in ServerTest.

## Packaged-JAR browser checks

Playwright Chromium against the actual release JAR and isolated H2, never the user's MySQL database.

- Color Game: passed; guest/sign-in/register, stacked chips, hidden outcomes, exact normal payouts, retries, reload, audio, English/Filipino, two players, wallet isolation and responsive widths 320/390/760/844/1440.
- History: passed; 20 result fixtures, exact color percentages, 10 columns per page, selection, refresh, empty state, translations and responsive dialogs.
- Jackpot: passed; real pool and HTTP settlement, exact tier payout, receipt/ack/reload, Creator-only funding, disabled Club isolation, phone landscape and translations.

- Phone landscape: passed at 390×844, 844×390, 667×375 and 360×800; usable betting/chip controls, History and Jackpot dialogs, denied native fullscreen fallback, a real accepted wager and layout cleanup when leaving Color Game. The check caught an oversized rotated Jackpot dialog; its bounds now use the logical landscape viewport.

Screenshots are in `docs/previews/v35/`. The final ZIP is checked for CRC errors, successful extraction, release checksum and exact client/JAR resource parity.

## Limitations

The checks use H2's MySQL compatibility mode, not a live MySQL instance on the user's Windows computer. Mobile checks emulate touch viewports in Chromium; physical Android/iPhone browser behavior and Safari were not tested. Browsers may refuse native orientation locking; the included CSS landscape fallback is used in that case. Jackpot results in integration tests use controlled database fixtures; production dice and tier draws use server randomness.
