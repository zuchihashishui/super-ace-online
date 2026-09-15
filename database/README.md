# Database schema V6 — LUCKY SEVEN

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
System management groups do not have usable login passwords. Sign in as Creator
to create actual Agents/Super Agents, move players and issue Club chips.

For an existing installation, normally restart the new server and let Flyway
apply missing migrations. Existing usernames, passwords and balances are retained.
The new default credentials apply to new installations; old accounts are not reset.

For a manual upgrade, stop the server, back up MySQL and check the installed version:

```sql
SELECT version, description, success FROM ace.flyway_schema_history ORDER BY installed_rank;
```

- Latest version 4: import `upgrade_v4_to_v6.sql`.
- Latest version 5: import `upgrade_v5_to_v6.sql`.
- Latest version 6: no upgrade is needed.
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
