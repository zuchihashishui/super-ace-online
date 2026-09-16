# V9 — Creator RTP settings and Game for all roles

- Player, Agent, Super Agent and Creator can play both Lobby and Club, including
  autoplay, while keeping their own role-specific management menus.
- Existing balances are preserved. Management accounts with zero Gold/chips
  still need sufficient funds for paid play; this update does not mint currency.
- Only Creator can access GET/PUT /api/rtp and the RTP settings panel in Game.
  The Game page has separate Lobby/Gold and Club/chips settings cards, each
  with its own target, observed statistics and Save button. Both can be edited
  regardless of the currently selected play mode. Saving one leaves the other
  unchanged. Non-Creators
  receive 403 from these endpoints; guests receive 401. Public RTP labels and
  obsolete scheduled-RTP text were removed from the UI.
- Both modes default to 97.00%. Settings accept 1.00–100.00% with up to two
  decimal places, for example 97.55. The database stores integer hundredths of
  one percent to avoid floating-point storage errors. Edits use revision checks
  and an audit log, and survive restarts.
- Changes apply to subsequent paid rounds. Existing Free Spins retain their
  original profile, including bonuses from older releases. Historical rounds
  are unchanged. RTP is a long-run target using the existing payout calibration,
  not a per-spin win probability or a guarantee of observed returns. Payout
  rounding and finite samples can differ from the configured target.
- The public paytable remains available and accurate; its payout factors are
  not treated as secret. The administrative target/statistics are Creator-only.

## Upgrade

Back up MySQL, stop the old server, replace the JAR and start with Java 21.
Flyway applies missing migrations automatically. Alternatively, use the SQL
upgrade file matching your current schema (for example upgrade_v8_to_v9.sql
or upgrade_v7_to_v9.sql) while the server is stopped. Do not import
database/super_ace.sql over an existing database; it is for fresh installations.

Source build: `cd server && mvn package` with JDK 21. The current prebuilt JAR
includes V8/V9, current client files and compiled backend classes.
