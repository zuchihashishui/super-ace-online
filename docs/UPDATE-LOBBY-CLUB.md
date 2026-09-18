# Lobby / Club update — schema V6

Registration requires username, password and matching repeat password. Passwords
need at least six characters. Every new account belongs to LUCKY SEVEN (686868).
New Players get 10,000 Lobby Gold and zero Club chips. Gold cannot be transferred
or converted into Club chips. Club balances and history from older releases remain
intact; they are not confiscated or reset by this update.

The game uses independent wallets, free-spin counters, autoplay jobs, revisions and
idempotency records in each mode. Requests carry `mode=LOBBY` or `mode=CLUB`; the
server selects the wallet and payout profile. Agent/Creator operations always use
Club chips. Stop autoplay and finish any current bonus before using the UI to
switch modes. Server autoplay continues independently of browser visibility.

RTP targets are fixed at 98% in Lobby and 97% in Club, using the existing empirically
calibrated payout model. These are long-run targets, not guaranteed session returns.
An old unfinished Club bonus keeps its original pinned payout profile until finished.
Club win/loss reports, rankings and weekly commissions exclude all Lobby Gold rounds.

Creator Give/Issue credits the recipient without consuming Creator balance. Agent
and Super Agent Give requires sufficient Club chips and a recipient in their own
hierarchy. Players cannot transfer or request chips. Existing chip history remains
readable. Transfers are transactional and duplicate request IDs cannot credit twice.

New receipts are stored on the server and checked by the Player UI approximately
every three seconds while running. The popup shows the amount, sender and Club.
Acknowledgment is saved so reopening the page does not show the same receipt again.
Offline Players receive unread notices on return; browser background throttling
can delay display. Modal forms take precedence over receipt popups.

Menu differences:

| Role | Interface |
|---|---|
| Player | Lobby/Club game, own history/results, Club ranking, profile |
| Agent | Own players, player reports, scoped chip transfers, commissions, profile |
| Super Agent | Agents and players in own hierarchy, moves/promotions, scoped transfers, reports/commissions |
| Creator | Club management, full hierarchy, promotions/moves, chip issuance, commission settings |

Fresh-install defaults: `zuchiha` (Creator), `zuchiha1` (Super Agent), `zuchiha2` (Agent), `zuchiha3` (Player). All use password `112357`.
See `database/README.md` and `database/super_ace.sql` for the complete SQL and upgrade paths.
If an old `.env.local` or `.env` exists, it remains authoritative; existing credentials
are not silently replaced. Update its Creator settings when preparing a fresh database.

Verification uses a disposable H2 database in MySQL compatibility mode; it does not
connect to the user's local MySQL. HTTP and DOM tests cover auth, mode switching,
receipt acknowledgment, denied chip requests and transfer authorization. Integration
checks cover fresh schema, V4 upgrade, SQL import with Flyway checksum validation,
concurrent spin retries, separate paid/free counters and hierarchy changes during bonuses.
The SQL import compatibility test only substitutes MySQL database selection and time
functions for H2 equivalents. A native MySQL integration run and physical phone visual
verification were not performed in this environment.
