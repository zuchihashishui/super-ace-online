# Crash — V19 application / V17 database

Crash is the next complete game after Color Game. Original space-flight art uses HTML/CSS/SVG, with responsive desktop/mobile controls, denomination chips, sound, a 20-round public history and a separate 20-bet personal history. All playable roles and guests can view the table; an enabled account is required to bet.

## Rules

One stake of 5–500 per round. The betting window lasts 10 seconds. The multiplier then increases exponentially from 1.00×. It may crash immediately. Manual cash out pays stake × the server multiplier, rounded down to 0.01. At the exact crash instant, the crash wins. Uncashed stakes are lost. Accepted bets cannot be cancelled. No side bets, progressive jackpot or auto-betting.

Optional auto cash out is selected before betting, from 1.01× to 99.99×; default 2.00×. This is a persisted server instruction and can settle while the browser is closed. It does not guarantee a win. The displayed live multiplier is an estimate; manual cash out uses server time after acquiring the wallet lock. Network/lock delay can cause a cash out to arrive too late.

The flight is capped at 100×. The server samples a uniform integer D in [1, 1,000,000] with SecureRandom and sets the crash point in hundredths to clamp(floor(97,000,000 / D), 100, 10,000). This is an independent model, not a claim to reproduce a commercial provider. For automatic target A (101–9999 hundredths), survival probability is floor(97,000,000 / (A+1)) / 1,000,000; gross payout additionally rounds down to cents. Example: at 2.00×, theoretical gross return for a whole-chip stake is 96.5174%. Quantization and ties mean this is not exactly 97%. Super Ace RTP settings do not alter Crash.

## Persistence and accounting

V17 adds persisted crash rounds, a transaction-locked table pointer, and indexed due times on both existing wager ledgers. Lobby and Club share flight outcomes but use separate stake totals, ledgers and wallets. No account identities are exposed by the public pool. Standard Club reports include accepted wagers and settled payouts.

Stake reservation, receipt storage and balance revision are one transaction. Reusing a request with different inputs fails. Wallet locks serialize duplicate bets/cashouts and scheduler settlement. Manual cash out uses the original wager ID, so retrying a lost response cannot pay twice. Auto settlement remains due after process restart; there is no in-memory-only wager state. Crashes/auto cashouts are derived from persisted timestamps.

The client saves pending actions per account and wallet, keeps their UUID on network failure, and discards stale poll responses that predate a mutation. Switching wallets/games is blocked while a local wager needs settlement or recovery. The server remains authoritative across multiple tabs.

## Update

Keep existing data and local configuration, stop the old server and run the new start.bat. Flyway applies V17. `database/super_ace.sql` is for fresh installs only. Existing V16 installations can alternatively use `database/upgrade_v16_to_v17.sql` with the server stopped; do not run manual migration after Flyway already applied it. Earlier SQL files remain available for earlier versions.

## Tests

62 backend tests passed, including all one million discrete random inputs, multiplier timing, payout rounding, concurrency, retry, wallet isolation and V16→V17 migration preservation. Browser tests use the actual release JAR and an isolated H2 database, with deterministic crash fixtures injected only by the test harness (no production fixture endpoint). Verification is not an external RNG audit. Actual MySQL, Windows and physical mobile devices were not available here.

Packaged-JAR browser suite passed at 320/390/760/844/1440 pixels: guest/registration, hidden crash data, lost response retry, reload, actual offline auto settlement with scheduler enabled, manual cashout, repeat cashout, wallet restrictions, Filipino and audio. Test-only SQL fixtures set flight outcomes and pre-mark the daily Gold grant so unrelated grants cannot affect stake assertions.
