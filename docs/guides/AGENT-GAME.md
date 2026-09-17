# Agent Game access

Agents have Game, Ranking, My players, chip history, player reports, weekly
commission and Account menus. They can spin and run autoplay in Lobby and Club,
while retaining approval and chip-transfer access for their subordinate Players.
Since V9, Creator and Super Agent can also play both modes and use autoplay.
Their management menus remain available. Super Agent personal rounds have no
Agent attribution; Creator personal rounds have neither Agent nor Super Agent
attribution. These personal rounds do not create Agent commission.

Lobby Gold and Club chips use the existing separate wallets. This update does
not issue Gold/chips or reset balances. An Agent with zero Lobby Gold cannot
place a paid Lobby spin. A Player promoted to Agent keeps their Gold. Club play
uses the same Club balance as transfers; spending chips in Game reduces the
amount available to send to Players. Server wallet locks serialize these actions.

Agent rounds are recorded against that Agent and their Super Agent, and appear
in reports and rankings. An Agent's own rounds are excluded from both commission
loss-base calculation modes. Historical closed commission records are unchanged.

V9 includes the V8 unassigned-Player and V9 RTP-settings migrations.
See BACKEND-TEST-RESULTS.md for current build and packaged-release verification.

Verification: client JavaScript syntax and diff checks passed. Added integration
tests for Agent manual spin, autoplay in both modes, retry idempotency, wallet
isolation, own/downstream transfers, denied cross-hierarchy operations, and
exclusion of personal losses from commission. Current execution results and
remaining limitations are recorded in BACKEND-TEST-RESULTS.md.
