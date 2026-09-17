# Dragon Tiger: responsive chip stacks and table totals (V14)

## Display
- The 5, 10, 15 and 20 chips have matching colours and denomination labels in the picker and on the betting table.
- Each confirmed click is one chip. Two accepted 5-chip clicks display a stack of two 5 chips, ×2, and a total stake of 10.
- Different denominations form separate stacks on each side. Decorative layers are capped at six per denomination to avoid covering controls; the multiplier, chip count and stake amount remain exact.
- Every side separates Your bet / chips placed from Others / bettors. The console also displays the current player's total amount and count across all sides.
- The chip selector is immediately above the betting areas. It does not float over or obscure the totals on small phones. PC and phone layouts share the same functionality.
- Pending requests remain labelled separately and do not inflate confirmed chip stacks or other-player totals.

## Other players
GET `/api/dragon-tiger/crowd?mode=LOBBY|CLUB&roundId=N` returns confirmed current-round totals grouped by side. A valid session identifies the viewer; Others always excludes that viewer. Guests see aggregated bets without account details.

The browser polls at 800ms while this game is visible, with at most one request in flight. Stale/error snapshots display unavailable/updating rather than invented totals. Round, currency and account changes discard mismatched responses. A returning tab resynchronizes.

Only monetary/count aggregates are returned. Other account IDs, names, individual bets, balances, payouts and hidden cards are not exposed. Bettors this round means distinct accounts that have placed a bet, not people merely connected or watching. Lobby Gold and Club chips are never combined.

## Database and release
V14 adds current-table lookup indexes to both ledgers. No balance, account or payout data is changed. `start.bat` lets Flyway apply V14; the fresh-install and versioned upgrade SQL files are generated from migrations. Do not import fresh-install SQL over an existing database.

The release includes updated client files, compiled server, migration and checksum.

## Verification
- 46 backend tests passed using Java 21 and disposable H2 in MySQL mode; focused checks cover viewer exclusion, distinct bettors, duplicate retries, separate currencies, public-response privacy and V13→V14 migration preservation.
- Chromium checks cover 320, 390, 760 and 1440px layouts, denomination stacks, chip counts, betting, sound, history, reload/retry and two independent authenticated sessions placing real bets.
- Example checked: A places 2×5 on Dragon, B places 20. A sees Own=10 / Others=20; B sees Own=20 / Others=10. Retrying B's request does not increase totals.
- Native Windows, physical mobile devices and the user's MySQL server were not exercised. No high-concurrency load capacity is claimed by the two-session check.
