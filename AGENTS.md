# Project maintenance requirements

- Keep client/ and server/ separate. Server uses Java 21, Spring Boot and MySQL.
- Every schema change requires a NEW Flyway migration; never rewrite an applied migration.
- Regenerate database/super_ace.sql and versioned upgrade SQL with tools/generate_database_sql.py after schema or seed changes. Keep default LUCKY SEVEN 686868 and fresh-install Creator zuchiha / 112357 consistent with bootstrap configuration. Do not reset existing credentials.
- Keep Lobby Gold and Club chips isolated, including autoplay, free spins, retries, reporting and transfer authorization. New Players start with 10,000 Lobby Gold and 0 Club chips.
- Creator may issue chips; Agent and Super Agent transfers require sufficient Club chips and hierarchy scope. Players have no request/transfer endpoint. Preserve receipt acknowledgment and idempotency.
- Maintain distinct role menus in English and Filipino, with mobile navigation.
- Compile changed server classes and include current client files and migrations in the release JAR. Validate the actual packaged release, not just source. Update the release checksum.
- Verify migration from existing data, auth, wallet isolation, transfer scope and duplicate requests for related changes. Record test limitations honestly.
