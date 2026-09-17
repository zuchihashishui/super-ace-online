# V35 — Color Game Lobby Jackpot

V35 completes the Lobby pilot proposed in V34. Existing History graphics, phone landscape layout and physical dice motion are preserved. The rules below are the project's own published rules, not a claim to reproduce the reference app's undisclosed mechanics.

## Rules version 1

- Lobby pool starts at **30,000 Gold**, once, through migration V20. Club has a separate disabled pool at zero chips.
- On settlement, **1% of accepted stakes** goes into the pool. Players pay no additional fee; base Color Game payouts remain unchanged. Fractions below one cent carry forward in the pool.
- Exactly **three BLUE dice** trigger a single shared prize. All players with an accepted stake in that round qualify, regardless of the color they bet on. There is no additional 10,000-Gold threshold.
- The server selects one tier for the round using SecureRandom. Chances below are conditional on a three-BLUE result, not per-bet win chances.

| Tier | Share of pool paid | Conditional tier chance |
| --- | ---: | ---: |
| Grand | 50% | 0.1% |
| Major | 20% | 0.9% |
| Minor | 5% | 9% |
| Mini | 1% | 90% |

The pool used includes that round's contribution. The prize is rounded down to cents and split by each player's total accepted stake in the round. The largest fractional remainders receive remaining cents, with account ID as a stable tie-breaker. Clicking more times with the same total stake does not increase a player's share. No award is issued when the rounded prize is zero. No prize is issued for an empty round.

For three independent fair six-color dice, three BLUE occurs with probability 1/216. History does not predict future results. The original 2×/3×/4× Color Game paytable is unchanged, and Super Ace RTP settings do not change Color Game or Jackpot.

Mini is **1% of the pool**, not the reference app's fixed 30,000 Gold. There is no automatic pool refill after a win. Creator can issue additional Lobby pool Gold from the Jackpot help dialog; this does not debit a personal wallet. The Club pool cannot be funded or enabled by this UI/API in V35.

## Player and Creator UI

- The pink cabinet shows the actual settled pool, including cents in its caption. It refreshes every two seconds while Color Game is open.
- `?` opens rules, conditional tier chances, the last 20 jackpot rounds and the signed-in player's last 50 awards.
- A jackpot receipt displays tier, round and credited amount. Acknowledgment is saved; closing/reloading cannot credit a second award. Unread awards remain available after disconnecting.
- Daily Color Game ranking includes base returns and jackpot awards. Club commission reports remain Club-only; the Lobby pilot does not alter them.
- English and Filipino, phone landscape, desktop, reduced-motion support and keyboard-close dialogs are included.

## Data and settlement

New migration: `server/src/main/resources/db/migration/V20__color_jackpot.sql`.

Tables: `color_jackpot_pools`, `color_jackpot_rounds`, `color_jackpot_bets`, `color_jackpot_awards`, `color_jackpot_journal`.

Accepted bets, wallet debits and jackpot participation commit together. Pool contribution, tier selection, awards, wallet credits, journal entries and settled flag commit in one transaction. Locks serialize settlement and Creator funding. Unique keys prevent duplicate round/player awards and duplicate funding requests. Seed, contribution, award and Creator funding have separate journal records. Jackpot rules are snapshotted by version at round creation; V35 does not expose live rule editing.

The scheduler settles closed rounds even without an online player. Public pool reads also catch up closed rounds. Outcomes are not exposed before betting closes. Existing rounds/bets from pre-V35 releases are not enrolled retroactively.

## Updating

1. Stop the old server and retain your existing external `application.properties` settings.
2. Replace project files with this release and run `start.bat` using Java 21 and your MySQL database.
3. Flyway applies V20 once. Do not import the full fresh-install SQL over an existing database.

`database/super_ace.sql` contains the complete fresh schema and seed data. `database/upgrade_v19_to_v20.sql` is available for the manual upgrade workflow; use one migration workflow consistently. Existing accounts, passwords, balances and game history are preserved.

## Validation

See `docs/test-results/V35-VALIDATION.md` for completed checks and limitations.
