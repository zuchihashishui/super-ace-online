# V27 — Color Game payout clarity, three-color limit and dice motion

The payout rule remains unchanged and includes the winning stake:
- One matching die: 2× the stake on that color.
- Two matching dice: 3×.
- Three matching dice: 4×.
- No match: zero.

For RED 10 + YELLOW 10 + WHITE 10, dice GREEN / RED / RED return RED 30, YELLOW 0, WHITE 0. Total wager is 30, total returned is 30 and net is zero. The balance returns to its pre-bet value. The client now labels all three amounts explicitly instead of showing net alone. A refund/break-even round does not trigger a profit celebration.

Each player can choose at most three distinct colors in each table round, independently for Lobby and Club. Repeat top-ups to those same colors remain allowed. Confirmed bets and the local pending queue both count toward the client limit. The server counts accepted colors while holding the player's wallet lock, so concurrent requests from different tabs cannot accept a fourth color. Request-ID retries are still resolved before the limit check and cannot charge twice. A rejected fourth color does not debit the wallet. Existing bets are not canceled retroactively.

Dice now drop with quadratic acceleration, bounce in shorter/lower arcs and tumble multiple times before friction slows the roll. Contact shadows tighten as dice approach the table. The sequence uses transform-only Web Animations and ends at the authoritative result; background tabs and reduced-motion preferences retain the instant-completion path.

No database schema change: remains V19. Existing installations use their current database; do not re-import the fresh-install SQL.

## Verification

- Java 21: 51 backend tests passed (48 ServerTest, 3 ColorGameEngineTest), zero failures/errors. Includes forced GREEN/RED/RED outcome for three 10-chip bets, exact wallet return, no duplicate settlement, rejected fourth color without debit, top-ups and player/wallet isolation.
- Actual release JAR: V27 browser scenario passed with live HTTP calls, the exact 30-chip return, no false profit celebration, client/server fourth-color rejection, pending queue limit, next-round reset, three animated 3D cubes and no horizontal overflow at widths 320/390/760/844/1440.
- Tests use isolated H2 in MySQL compatibility mode, not the user's MySQL database. Responsive checks use Chromium emulation, not physical phones.

- Existing Color Game and recovery browser suites also passed: guest/register, stacked chips, retry/reload, history, Filipino, audio, multiplayer totals, wallet isolation, delayed responses, lost responses and offline recovery. All three selected browser suites passed against the packaged release JAR.
