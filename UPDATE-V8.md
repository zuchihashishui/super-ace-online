# V8: remove Agent ownership while retaining Club membership

Historical V8 delivery notes below. V9 includes these changes and a rebuilt,
tested JAR; see UPDATE-V9.md and BACKEND-TEST-RESULTS.md for current validation.

Request Join always creates a PENDING request, even for Players without an
Agent. Only the requested Agent's Approve action assigns the Player. Reject
leaves the Player unassigned. The UI now explicitly states that approval is
required; the default-Agent regression test covers pending/rejected states.

All Agents, including the default Agent, can remove their own Players.
Removal clears parent_id and leaves club_id, wallets, sessions and game state
unchanged. Pending membership requests are cancelled. The Player can ask to
join an Agent again and remains visible to Creator.

Unassigned Players can play both modes. New rounds have NULL Agent/Super Agent
attribution and remain visible in Player/Creator reports, without paying Agent
commission. Historical rounds and settlements keep their original attribution.

For existing V7 databases, let Flyway apply V8 on startup, OR stop the server,
back up the database and apply database/upgrade_v7_to_v8.sql once. Do not use
database/super_ace.sql on an existing database; it is for fresh installs.

The prior BACKEND-TEST-RESULTS.md records V7 validation, not this update.

Verification: JavaScript syntax and git diff checks passed. A backend regression
test was added for default-Agent removal, unchanged Club/wallet, scope denial,
unassigned spins in both modes, Player reports and rejoining. The existing
removal test was updated to expect no Agent after removal. Java tests and release
build could not run: only Java 17 is installed and Java 21 downloads failed.
This delivery excludes the obsolete V7 release JAR. With Java 21 and Maven:

```sh
cd server
mvn package
java -jar target/super-ace-online-13.0.0.jar
```

Native MySQL migration and runtime testing remain required before deployment.
