# Dragon Tiger chip betting — V13

- Select 5, 10, 15 or 20, then tap a betting area. Every tap submits a new bet.
- Accepted bets accumulate independently on Dragon, Tiger and Tie. Pending amounts are labelled separately until the server confirms them.
- Betting lasts 10 seconds. A server deadline, checked after acquiring the wallet lock, rejects late bets. Cards then reveal for 5 seconds.
- Chip-pop feedback, selected-chip glow, a countdown progress bar, final-three-second pulse/ticks, card flips and winner highlighting use the existing sound preference and respect reduced motion.
- Stake debit happens on acceptance; payout happens only after closing. Before closing, neither outcomes nor early winnings are returned.
- The scheduled server job pays disconnected players. Transactions and settled flags prevent duplicate credits. Unsettled bets persist through restart.
- Rapid clicks are queued with separate UUIDs. Failed responses can be retried with the same UUID; reload restores the pending queue and retrieves accepted bets from the server.
- Lobby Gold and Club chips remain separate. Paytable and Super Ace logic are unchanged.

## Updating

Back up MySQL, stop the previous server, replace the application files while retaining your connection configuration, then run `start.bat`. Flyway applies V13. Do not re-import the fresh-install SQL into an existing database.

V13 removes the one-bet-per-round unique indexes, replaces them with lookup indexes and adds settlement flags. Old bets default to settled, so upgrading cannot pay them again. `database/super_ace.sql` and the matching `upgrade_vN_to_v13.sql` files include the changes.

## Verification

- 43 Java 21 backend tests: migrations, wallet isolation, auth/CSRF, repeated/concurrent requests, multi-bet totals, insufficient funds, closed rounds and exactly-once settlement.
- Live HTTP and jsdom regression checks: registration/login/logout, transfers, mode switching, RTP and Super Ace.
- Chromium: chip values, five rapid bets over three sides, hidden pre-close outcomes, totals after reload, response loss/retry, reveal, separate currencies, English/Filipino and 320/390/760/1440px layouts.
- Background-job check: winnings arrive without polling the game, no duplicate payout, no Club balance change.
- Tests used disposable H2 in MySQL compatibility mode. Native Windows and the user's MySQL instance were not exercised.
