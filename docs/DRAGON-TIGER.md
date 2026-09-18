# Dragon Tiger — custom two-card variant (schema V10)

## Rules
Each round uses a fresh, uniformly shuffled 52-card deck. Four distinct cards are dealt in order Dragon, Tiger, Dragon, Tiger using SecureRandom and partial Fisher–Yates. No third card is drawn.
A=1, 2–9=face value, 10/J/Q/K=0; the score is the sum modulo 10. Highest score wins; equal scores are Tie.
This is the user's requested variant, not standard one-card Dragon Tiger or full Baccarat.

Choose one of Dragon, Tiger or Tie per round; bet 5–500, default 5, in 0.01 units.

| Bet | Result | Gross return (including stake) |
|---|---|---|
| Dragon / Tiger | Chosen side wins | 1.95 × stake |
| Dragon / Tiger | Tie | Full stake returned |
| Dragon / Tiger | Other side wins | 0 |
| Tie | Tie | 9 × stake |
| Tie | Dragon / Tiger wins | 0 |

Amounts are stored as integer cents; gross return is rounded half-up once per round.
Dragon/Tiger profit is 0.95×, Tie profit is 8×. The paytable is displayed before betting.
Probabilities are fixed and independent of account, balance or play history. Super Ace's configurable RTP does not change this game.
Exact enumeration of ordered cards gives 643,192 tied outcomes among 6,497,400 possible deals: tie probability 9.8992212%; Dragon and Tiger each 45.0503894%.
Before cent rounding, theoretical returns are 97.7474805% for either side and 89.0929910% for Tie. These are mathematical averages, not personal return guarantees. There is no combined RTP without specifying a betting mix.

## Integration
- Every role may play. Guests can view the table and rules but cannot place a bet.
- Lobby uses lobby_wallets; Club uses wallets. No movement between currencies.
- Authenticated, CSRF-protected POST /api/dragon-tiger/rounds?mode=LOBBY|CLUB; own last 50 results through GET of the same route.
- Atomic debit, payout and immutable ledger write under the existing wallet row lock.
- Duplicate UUID returns the original response; changed side/stake or cross-game reuse is rejected.
- Expected wallet revision prevents two new concurrent bets spending the same snapshot.
- Active Super Ace autoplay or unplayed free spins blocks Dragon Tiger bets in that mode.
- Pending client requests survive reload and retry the same UUID. Server results are committed before animation.
- Existing Club reports, rankings and weekly commission include both games. Lobby remains excluded from these Club totals. All prior commission rules remain intact.
- This release uses individual rounds on each player's table, not a synchronized shared live-dealer room.
- English and Filipino; responsive cards, score reveal, sound (existing sound toggle), reduced-motion support and per-mode personal history.

## Install / upgrade
Use the existing start.bat; the new JAR retains its filename.
Flyway automatically applies V10 to existing databases. Do not re-import super_ace.sql into an existing database.
For a new database, database/super_ace.sql includes all migrations and the four default accounts.
Manual V9 upgrade alternative: database/upgrade_v9_to_v10.sql with the server stopped.
Source changes to the schema must be followed by tools/generate_database_sql.py.

## Verification
37 backend tests passed on Java 21 / disposable H2, including scoring, all payout branches, cent rounding, 10,000 generated decks, simultaneous duplicate requests, mode isolation, auth/CSRF, reporting, and preservation of existing data across migrations through V10.
Live HTTP and jsdom checks cover account flows, Super Ace regressions and separate RTP settings.
Real Chromium checks cover guest preview, phone/desktop layouts, wagers, dropped response followed by reload/retry, isolated wallets, language switching, and return to Super Ace.
Native Windows and the user's local MySQL server are not available in this environment.
