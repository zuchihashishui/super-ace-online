# Database schema V14 — LUCKY SEVEN

V14 adds lookup indexes for Dragon Tiger's current-table totals. Data and balances are preserved. Start the new server to migrate automatically, or use the matching `upgrade_vN_to_v14.sql` with the server stopped.

`super_ace.sql` contains the complete MySQL 8+ database schema, indexes, constraints,
LUCKY SEVEN club (public ID 686868), initial management hierarchy, wallets and
Flyway version/checksum records. This is the maintained full database file.

Fresh installation:

```sh
mysql -u root -p < database/super_ace.sql
```

Fresh installation creates these four accounts, all using password **112357** and stored as BCrypt hashes:

| Username | Role | Parent |
|---|---|---|
| `zuchiha` | Creator | — |
| `zuchiha1` | Super Agent | zuchiha |
| `zuchiha2` | Agent | zuchiha1 |
| `zuchiha3` | Player | zuchiha2 |

The Player starts with 10,000 Lobby Gold and 0 Club chips.
All four seeded accounts can sign in. Creator can create accounts, move players and issue Club chips.

For an existing installation, normally restart the new server and let Flyway
apply missing migrations. Existing usernames, passwords and balances are retained.
The new default credentials apply to new installations; old accounts are not reset.

For a manual upgrade, stop the server, back up MySQL and check the installed version:

```sql
SELECT version, description, success FROM ace.flyway_schema_history ORDER BY installed_rank;
```

- Latest version 4–13: import the matching `upgrade_vN_to_v14.sql`.
- Latest version 12: no upgrade is needed.
- Other versions: use the server's Flyway migration path.

Do not import the fresh-install file over existing tables or run a manual upgrade
after Flyway has already applied it. The upgrade files record their checksums so
the next server startup validates them without reapplying the same migrations.

V5 adds separate Lobby wallets, round history and autoplay. Existing Club balances
remain intact. Existing Players receive 10,000 Lobby Gold once during migration.
V6 closes pending chip requests and refunds reserved withdrawals, then adds receipt
notifications. Only management chip transfers remain available through the API.

For subsequent versions, add a new Flyway migration and run
`python3 tools/generate_database_sql.py`; update the generator's version label and
supported upgrade ranges when increasing the schema version. Never edit a migration
already applied to users' databases.

V10 labels existing ledger rows as SUPER_ACE and adds indexes for Dragon Tiger history. Wallets, passwords and earlier Flyway migrations are unchanged. Historical upgrade files remain for earlier releases; use a file ending in _to_v14.sql for this release.

V11 changes untouched Super Ace RTP defaults to Lobby 100% and Club 97.5%. Creator-saved settings, wallet balances and active bonus profiles are preserved.

V12 adds the server-stored Dragon Tiger table cycle and one-bet-per-player-per-round ledger keys. Existing balances, account credentials, Super Ace rounds and Dragon Tiger history are preserved.

V13 replaces those unique round keys with non-unique lookup indexes so multiple bets may share one table round. New bets reserve stakes until closing, then settle exactly once. Existing rows default to settled, preserving all prior payouts. Use `upgrade_v12_to_v14.sql` only when upgrading manually from V12; otherwise start the server and let Flyway apply the migration.
