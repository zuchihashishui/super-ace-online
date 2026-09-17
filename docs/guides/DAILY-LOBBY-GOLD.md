# Daily Lobby Gold — V15

Every existing account, including Agent, Super Agent and Creator, receives an additive 10,000 Gold per calendar day in Asia/Manila. Club chips are never changed. The scheduler runs five seconds after startup, then once per minute, including for offline users. Accounts created after a run receive the reward on the next run. The existing registration gift is separate, so a new Player can have 20,000 Gold on their first day before spending.

A durable reward row stores the account, reward date, amount, and notification acknowledgment. A wallet row lock plus a unique account/date key prevent duplicate grants across retries, restarts and server instances. Wallet credit and reward receipt commit in one transaction. The wallet revision increments. Unread notifications remain available on the next login, in English or Filipino. Acknowledgment does not grant additional Gold.

The server must be running to distribute rewards. On restart it grants the current day only; days when the server was completely offline are not backfilled. `ace.jobs-enabled=false` disables the scheduler for tests.

Existing databases migrate automatically with Flyway V15. Do not reimport the fresh-install SQL into a populated database. `database/super_ace.sql` and the versioned upgrade scripts include V15.

Verification uses H2 in MySQL compatibility mode, concurrent grant calls, next-day grants, receipt ownership and acknowledgment, wallet isolation, V14 migration preservation, and a browser against the release JAR. Native MySQL/Windows execution is not available in the test environment.
