# Current update: Dragon Tiger chip stacks and multiplayer totals, schema V14

Chip denominations now stack visually on each side with exact counts and amounts. Other players' confirmed stakes appear separately, excluding the current player and keeping Lobby/Club separate. Responsive controls sit directly above betting areas without hiding totals. See [multiplayer notes](docs/guides/DRAGON-TIGER-MULTIPLAYER.md). Flyway applies V14 indexes automatically.

The latest presentation adds a compact emerald table, mobile chip controls, chip flights, round-state banners, dedicated sound/volume controls with saved preferences, full screen where supported, and tappable 20-game history for reviewing cards. See [presentation notes](docs/guides/DRAGON-TIGER-PRESENTATION.md). The two-card rules and paytable are unchanged.

The history panel now shows the last 20 completed table games, one row per game, combining all chip clicks into per-side stakes and total bet/payout/net. Guests can view public results. Cards open automatically after the 10-second countdown; there is no Deal Card button. This history update requires no new database migration.

Choose a 5, 10, 15 or 20 chip and tap Dragon, Tiger or Tie; every tap adds a bet to that side. Multiple sides are allowed. The synchronized table has 10 seconds for betting, then 5 seconds for card reveal. Stakes are debited on acceptance; winnings are credited only after betting closes, including while offline. Each click has an idempotent request ID. Flyway applies V13 automatically without replaying old payouts.

Project documentation is grouped under [docs](docs/README.md). `README.md` remains the project entry point and `AGENTS.md` remains at root for maintenance rules.

# Current update: Super Ace Scatter awards 10 Free Spins

Three or more Scatter symbols on the initial board now award 10 Free Spins. The payout calibration was updated so Creator RTP targets remain valid. No database migration is required.

# Current update: Super Ace RTP defaults, schema V11

Lobby defaults to 100%; Club defaults to 97.5%. Migration V11 updates untouched defaults only, preserving any Creator-saved settings. Dragon Tiger payouts are unchanged. Run start.bat to apply the migration automatically.

## Previous update: Dragon Tiger (two-card variant), schema V10

See [DRAGON-TIGER.md](docs/guides/DRAGON-TIGER.md) for rules, payouts, integration and tests.
The Game menu now offers Super Ace and Dragon Tiger, for all four roles.
Both use the existing separate Lobby Gold and Club chip wallets.
Dragon Tiger has fixed payouts; Creator RTP settings apply only to Super Ace.
Start the new server to apply migration V10 automatically. Preserve your external configuration.
The release filename remains unchanged for compatibility with start.bat.

## Previous update: Creator RTP settings, schema V9

Windows: start.bat and start-local.bat read the external
`server/src/main/resources/application.properties` directly, without loading
dotenv files. Local MySQL defaults are root / 123456.
See [WINDOWS-CONFIG.md](docs/guides/WINDOWS-CONFIG.md) for details.

See [UPDATE-V9.md](docs/guides/UPDATE-V9.md). All four roles can play Lobby and Club.
Only Creator can read/edit RTP settings. Defaults are now Lobby 100% / Club 97.5%, with
independent settings supporting two decimal places. V8 Agent removal and
approval changes are included. See BACKEND-TEST-RESULTS.md for validation.

Agents can now play Game in addition to managing their Players.
See [AGENT-GAME.md](docs/guides/AGENT-GAME.md) for wallets, commission treatment and test status.

See [AGENT-MEMBERSHIP.md](docs/guides/AGENT-MEMBERSHIP.md) for joining an Agent, approval,
scope checks and V6-to-V7 database upgrades. See [BACKEND-TEST-RESULTS.md](docs/guides/BACKEND-TEST-RESULTS.md)
for current backend verification and release details.

Read [UPDATE-LOBBY-CLUB.md](docs/guides/UPDATE-LOBBY-CLUB.md) for current behavior and [database/README.md](docs/guides/DATABASE.md) for SQL installation/upgrades. Full SQL: `database/super_ace.sql`. Fresh-install Creator: `zuchiha` / `112357`.

Registration now has three fields. New Players receive 10,000 Lobby Gold and zero Club chips. Player chip requests are disabled; management transfers produce receipt notifications. The notes below document earlier releases and are superseded by this update where they differ.

# Super Ace Online V13

**Cập nhật đăng ký:** chỉ Username + Password (tối thiểu 6 ký tự), không cần mã Agent.
MySQL đã chạy sẵn trên Windows: chạy `start-local.bat`. Xem [SIMPLE-AUTH.md](docs/guides/SIMPLE-AUTH.md).

Bắt đầu với [UPGRADE-V13.md](docs/guides/UPGRADE-V13.md): cấu hình localhost, MySQL, JWT, đăng ký, phân cấp, chuyển chip và lịch RTP.

- Source giao diện: `client/`
- Spring Boot Java 21: `server/`
- JAR: `release/super-ace-online-13.0.0.jar`
- Kết quả mô phỏng: [RTP-REPORT.md](docs/guides/RTP-REPORT.md)
- Kiểm thử: [VERIFICATION.md](docs/guides/VERIFICATION.md)

Game độc lập dùng chip ảo, lấy cảm hứng từ Super Ace. Không phải sản phẩm của JILI.
