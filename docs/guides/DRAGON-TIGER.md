# Dragon Tiger — custom two-card variant (schema V13)

## Rules
Each round uses a fresh, uniformly shuffled 52-card deck. Four distinct cards are dealt in order Dragon, Tiger, Dragon, Tiger using SecureRandom and partial Fisher–Yates. No third card is drawn.
A=1, 2–9=face value, 10/J/Q/K=0; the score is the sum modulo 10. Highest score wins; equal scores are Tie.
This is the user's requested variant, not standard one-card Dragon Tiger or full Baccarat.

Select a 5, 10, 15 or 20 chip (default 5), then tap Dragon, Tiger or Tie. Each tap places an additional bet; accepted totals appear on each side. Multiple sides may be selected in the same round. Accepted bets cannot be cancelled. The API retains its 5–500 per-request range for compatibility.

| Bet | Result | Gross return (including stake) |
|---|---|---|
| Dragon / Tiger | Chosen side wins | 1.95 × stake |
| Dragon / Tiger | Tie | Full stake returned |
| Dragon / Tiger | Other side wins | 0 |
| Tie | Tie | 9 × stake |
| Tie | Dragon / Tiger wins | 0 |

Amounts are stored as integer cents; gross return is rounded half-up per accepted bet, then summed.
Dragon/Tiger profit is 0.95×, Tie profit is 8×. The paytable is displayed before betting.
Probabilities are fixed and independent of account, balance or play history. Super Ace's configurable RTP does not change this game.
Exact enumeration of ordered cards gives 643,192 tied outcomes among 6,497,400 possible deals: tie probability 9.8992212%; Dragon and Tiger each 45.0503894%.
Before cent rounding, theoretical returns are 97.7474805% for either side and 89.0929910% for Tie. These are mathematical averages, not personal return guarantees. There is no combined RTP without specifying a betting mix.

## Integration
- Every role may play. Guests can view the table and rules but cannot place a bet.
- Lobby uses lobby_wallets; Club uses wallets. No movement between currencies.
- Authenticated, CSRF-protected POST /api/dragon-tiger/rounds?mode=LOBBY|CLUB. GET returns all individual bets from the player's last 20 completed table games, so multiple clicks are never truncated mid-game.
- Public GET /api/dragon-tiger/table/history returns the latest 20 completed, recorded table games, never an unrevealed outcome. The UI shows one row per game with winner, both scores, per-side stakes, total bet, payout and net result for the selected wallet. Guests can see table results without account data. No Deal Card button is used: the 10-second timer controls dealing.
- Atomic stake debit and pending ledger write under the existing wallet row lock. After closing, settlement locks the wallet, credits payouts and marks each bet settled in one transaction. Repeated settlement never pays twice. Background settlement continues without a connected browser and resumes after restart.
- Duplicate UUID returns the original response; changed side/stake or cross-game reuse is rejected.
- Expected wallet revision prevents two new concurrent bets spending the same snapshot.
- Active Super Ace autoplay or unplayed free spins blocks Dragon Tiger bets in that mode.
- Every server table has a 10-second betting phase and a 5-second reveal phase. All viewers see the same four cards.
- Multiple bets are accepted per player, currency mode and table round. Only stakes are deducted during betting; neither outcome nor payout is exposed before closing. GET /api/dragon-tiger/bets?mode=LOBBY|CLUB&roundId=N restores all of the authenticated player's bets.
- Pending client requests survive reload and retry the same UUID. The client catches up correctly after a hidden or suspended browser tab.
- Existing Club reports, rankings and weekly commission include both games. Lobby remains excluded from these Club totals. All prior commission rules remain intact.
- The table is synchronized by server time and stored in MySQL. A viewer without a bet still sees the cards at reveal.
- English and Filipino; responsive cards, score reveal, sound (existing sound toggle), reduced-motion support and per-mode personal history.

## Install / upgrade
Use the existing start.bat; the new JAR retains its filename.
Flyway automatically applies V13 to existing databases. Do not re-import super_ace.sql into an existing database. V12 bets remain marked settled and are never paid again.
For a new database, database/super_ace.sql includes all migrations and the four default accounts.
Manual upgrade alternative: use the matching database/upgrade_vN_to_v13.sql with the server stopped.
Source changes to the schema must be followed by tools/generate_database_sql.py.

## Verification
44 backend tests passed on Java 21 / disposable H2, including a 22-game/66-bet fixture verifying the 20-game history limit, hidden future outcomes, multiple bets, deferred and repeated settlement, insufficient funds, late bets, scoring, payout branches, 10,000 generated decks, simultaneous duplicate requests, mode isolation, auth/CSRF, reporting and preservation of data through V13.
Live HTTP and jsdom checks cover account flows, Super Ace regressions and separate RTP settings.
Real Chromium checks cover the 10-second clock, hidden cards before close, synchronized automatic reveal with or without a bet, phone/desktop layouts, dropped response followed by reload/retry, isolated wallets, language switching, and return to Super Ace.
Native Windows and the user's local MySQL server are not available in this environment.
