# Color Game — release V18, database V16

Color Game joins Super Ace and Dragon Tiger in the main/mobile menu. Guests can view the table but must sign in to bet. All enabled roles can play. Lobby Gold and Club chips remain physically isolated.

## Rules in this implementation

Each 15-second shared round has a 10-second betting window and a 5-second reveal window. Three independent dice each have six equally likely faces: Yellow, White, Pink, Blue, Red and Green. The server uses SecureRandom and stores the shared result; the client never chooses outcomes. The result is not returned before betting closes.

Select a denomination (5, 10, 20, 50 or 100), then click a color. Each click is another chip and can be placed on multiple colors. Stakes are debited immediately. Accepted bets cannot be cancelled.

| Matching dice | Total returned, including stake |
|---|---|
| 0 | 0 |
| 1 | 2 × stake |
| 2 | 3 × stake |
| 3 | 4 × stake |

These are this project's explicit rules; the reference screenshots alone do not establish another app's full paytable. There is no progressive jackpot or automatic betting. The pink banner displays the actual three-match return, not a fabricated jackpot. For fair independent dice, the theoretical return for one color is 199/216 (about 92.1296%). Super Ace RTP settings do not change Color Game.

## UI and shared play

The interface follows the supplied reference's pink dice case, wooden six-color board, warm chip console and purple/gold framing. Responsive desktop, portrait and landscape layouts use live HTML/CSS. Dice shaking, winner highlights, chip stacks and sound respect mute/reduced-motion preferences. Colors also have names and distinct symbols.

Each color shows the current user's total wager, denomination stacks and aggregated bets from other players. Other players' account IDs and individual stakes are not exposed. Daily returns uses actual settled Color Game gross payouts for the selected wallet since midnight Asia/Manila. It includes returned stakes, as labeled. No invented ranking or player counts.

The history dialog shows 20 completed games, the three dice for each game, own stake/net, and color frequencies over those completed dice. Frequencies are descriptive, not predictions.

## Accounting and recovery

Wagers use unique request IDs, wallet row locks and optimistic revisions. A repeated request returns the existing receipt without debiting again. Reusing a request for a different color, amount, round or game is rejected. Queue recovery survives reload in the same browser session; closed unaccepted bets are rejected rather than moved to another round. Payouts are applied once via transactional settlement and the server scheduler, including when a browser closes.

V16 adds color_game_rounds and cg_settled markers/indexes in both ledgers. Existing Super Ace/Dragon Tiger results, wallets and daily Gold receipts are retained. Common Club reporting/ranking continues to include recorded Color Game wagers and payouts. Lobby is excluded from Club reporting.

The client reads the wallet after settlement, and shared wallet updates ignore older revisions for the same user/mode to prevent stale UI balances. Switching games or wallets is blocked while the local wager queue or unsettled color bets need recovery.

## Upgrade and verification

Stop the previous server, keep its database and configuration, and launch the new start.bat. Flyway applies V16. Do not import the fresh-install database/super_ace.sql into a populated database. Manual upgrades are available in database/upgrade_v15_to_v16.sql and matching older-version scripts.

Backend tests cover all 216 dice combinations for all six choices, 10,000 seeded rolls, payouts, hidden outcomes, retries, settlement idempotency, wallet isolation, guest access and migration preservation. Browser tests run against the packaged JAR at 320, 390, 760, 844 landscape and 1440 pixels, including registration, multiple bets, reload, two players, history, language and sound. Test database: H2 in MySQL compatibility mode. Actual MySQL, native Windows and physical phones were not available for execution here.

## V18 recovery fixes

Polling now commits table, bets, crowd and wallet as one coherent snapshot. A response started before a new local wager cannot erase accepted chip stacks. A table snapshot older than 4.5 seconds disables betting until connection recovery. The server remains the authority for deadlines.

Authentication failures retain pending wager IDs for safe retry after sign-in. User/wallet changes cancel stale dice animation callbacks, and overlapping history requests cannot replace newer history. Switching language after reveal preserves the actual result and net amount.

The additional `tools/color_game_recovery_browser.cjs` fault-injection suite verifies delayed responses, a server-accepted wager whose response is lost, single charging on retry, offline betting lock and reconnection. It also checks the Dragon Tiger personal win effect for profit, loss, refund, duplicate result and reduced-motion cases. Dragon Tiger now has a net-win banner, gold effects and sound for personal profit.

No database schema changes in V18; database remains V16. Existing V17 installations need only the updated application files. Keep your existing database and local configuration.
