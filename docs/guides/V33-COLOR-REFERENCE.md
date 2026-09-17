# V33 — Color Game reference graphics

The presentation follows the supplied reference: solid-color dice faces, an ivory dice shell, pink/orange case, wooden six-color betting board, recent-result strip, purple ranking banner with medal positions and initials, and a pink Jackpot cabinet. The V32 rigid-body animation and server payout rules are unchanged.

Each tile shows your confirmed stake at the top, gold coin stacks in the center and the confirmed table total at the bottom. Other players’ total is also shown separately. Compact K/M amounts keep large totals readable. Names, balances and ranking amounts come from the server. Ranking remains daily gross returns (including returned stakes), not net profit. History shows the latest completed three-dice result and opens the existing last-20-game dialog.

## Jackpot status

The Jackpot cabinet is a visual addition only. It explicitly displays “Jackpot not active” and dashes instead of an invented prize. Its help dialog explains that winning conditions and prize rules are not configured. The three blue decorative tiles do not establish a winning rule. There is no jackpot contribution, payout, balance debit or database change in this release. Funding, eligibility, trigger and payout rules must be agreed before enabling actual awards.

## Run

Stop the old server, replace the program files with this package, preserve your database/configuration and run start.bat. Reload with Ctrl+F5. Client cache tag: 39-reference. Do not re-import the SQL file for this UI update. Database schema remains V19.

## Validation

The actual Java 21 release JAR passed color3d, color27 and color browser suites against isolated H2. These cover real WebGL physics and final color mapping, stable top faces, collision motion, cancellation and GPU-loss fallback; exact payouts, three-color limit and queued bets; registration, chips, retries/reload, history, Filipino, sound, two-player totals and Lobby/Club isolation. The updated suite also checks Jackpot help, recent history length, own/table totals and responsive layouts at 320, 390, 760, 844 and 1440 pixels. The final mobile layout places chip selection before Ranking and passed the final color regression rerun against the rebuilt release JAR.

Screenshots were inspected on desktop and mobile. Tests do not measure performance on every physical phone and do not connect to the user's local MySQL. No backend gameplay rules changed.
