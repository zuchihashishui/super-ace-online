# Agent membership update

Player: Account > Join an Agent > enter the six-digit Agent ID.
Agent: My players > Membership requests > Approve or Reject.
Only the requested Agent can decide. Approval changes the Player's parent;
Lobby Gold and Club chips stay with the Player. Existing chip movement scope
checks immediately reject transfers by the previous Agent.

Agent: My players > Remove player > confirm the Player name and public ID.
The Player has no Agent (parent_id becomes NULL), stays in their existing Club, stays enabled
and signed in, and retains both wallets, free spins and autoplay. Existing round
and commission history is not rewritten. Pending membership requests are cleared
to REMOVED so the Player can apply again. Removal uses a unique request ID;
replaying an old removal after the Player rejoins cannot remove them again.
The default club Agent can also remove their Players. The old Agent and Super
Agent lose current management/transfer scope. Creator retains access. Future
rounds have no Agent attribution until another join request is approved; past
round attribution is preserved. Players can keep playing Lobby and Club.

My players includes a Club chips column. /api/accounts now supplies scoped
clubChipsCents values from Club wallets; Lobby Gold is excluded. The table reloads
after a successful chip transfer/withdrawal, and its Refresh button retrieves
current balances after Players spin. The displayed balance is a snapshot; the
transfer endpoint rechecks the available balance inside its transaction.
No new schema migration is needed for removal or the Club balance column.

Agent tables show their Players. Super Agent tables show their Agents and
Players. Creator tables show all accounts. The manager's own account remains
in the API result for parent selectors but is excluded from subordinate tables.

## Database

Existing schema V6: start the newly built server to apply Flyway V7, or stop
the server and import database/upgrade_v6_to_v7.sql once.
Fresh installs: database/super_ace.sql includes V7.
Do not import the fresh-install SQL over an existing database.

## Build and validation

Use a working JDK 21 and Maven:

```sh
cd server
mvn package
```

JavaScript syntax and git diff checks passed. New integration tests cover
approval, rejection, duplicate requests, unauthorized approval, unchanged
wallets, new/old Agent transfer scope and duplicate chip transfers.
Removal and scoped balance tests were also added, including replay after rejoin,
unchanged wallets/session/autoplay, old Agent access denial, and balance refresh.
See BACKEND-TEST-RESULTS.md for current execution results, the rebuilt release,
and remaining database verification limitations.

GitHub Actions workflow: Agent membership checks. On main pushes it checks
JavaScript, runs the full backend suite with Java 21 and H2, then
packages the current source. A successful run provides the downloadable
super-ace-membership-java21 artifact. Native MySQL and visual UI testing remain
separate checks; workflow creation alone is not evidence that tests passed.
