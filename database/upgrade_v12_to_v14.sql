-- ONLY for schema V12. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

-- V13__dragon_tiger_multiple_bets.sql
DROP INDEX ledger_player_game_round ON round_ledger;
DROP INDEX lobby_ledger_player_game_round ON lobby_round_ledger;
CREATE INDEX ledger_player_game_round_lookup ON round_ledger(player_id,game_type,game_round_id);
CREATE INDEX lobby_ledger_player_game_round_lookup ON lobby_round_ledger(player_id,game_type,game_round_id);
-- Existing V12 rounds were paid immediately. Never pay those a second time.
ALTER TABLE round_ledger ADD COLUMN dt_settled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE lobby_round_ledger ADD COLUMN dt_settled BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX ledger_dt_pending ON round_ledger(dt_settled,game_round_id);
CREATE INDEX lobby_ledger_dt_pending ON lobby_round_ledger(dt_settled,game_round_id);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(13,'13','dragon tiger multiple bets','SQL','V13__dragon_tiger_multiple_bets.sql',-2052350647,CURRENT_USER(),0,1);

-- V14__dragon_tiger_table_totals.sql
-- Current-round totals across all bettors, kept separate for each currency wallet.
CREATE INDEX ledger_dt_table_totals ON round_ledger(game_type,game_round_id);
CREATE INDEX lobby_ledger_dt_table_totals ON lobby_round_ledger(game_type,game_round_id);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(14,'14','dragon tiger table totals','SQL','V14__dragon_tiger_table_totals.sql',-854339490,CURRENT_USER(),0,1);
