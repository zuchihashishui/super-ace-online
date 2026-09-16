-- ONLY for schema V9. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

-- V10__dragon_tiger.sql
ALTER TABLE round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
ALTER TABLE lobby_round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
CREATE INDEX ledger_game_player ON round_ledger(player_id,game_type,wallet_revision);
CREATE INDEX lobby_ledger_game_player ON lobby_round_ledger(player_id,game_type,wallet_revision);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(10,'10','dragon tiger','SQL','V10__dragon_tiger.sql',1445817963,CURRENT_USER(),0,1);
