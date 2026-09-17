-- ONLY for schema V8. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

-- V9__creator_rtp_settings.sql
CREATE TABLE rtp_settings (
 mode VARCHAR(10) PRIMARY KEY,
 target_bps INTEGER NOT NULL,
 revision BIGINT NOT NULL DEFAULT 0,
 CHECK(target_bps >= 100 AND target_bps <= 10000)
);
INSERT INTO rtp_settings(mode,target_bps) VALUES('LOBBY',9700),('CLUB',9700);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(9,'9','creator rtp settings','SQL','V9__creator_rtp_settings.sql',135689784,CURRENT_USER(),0,1);

-- V10__dragon_tiger.sql
ALTER TABLE round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
ALTER TABLE lobby_round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
CREATE INDEX ledger_game_player ON round_ledger(player_id,game_type,wallet_revision);
CREATE INDEX lobby_ledger_game_player ON lobby_round_ledger(player_id,game_type,wallet_revision);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(10,'10','dragon tiger','SQL','V10__dragon_tiger.sql',1445817963,CURRENT_USER(),0,1);

-- V11__super_ace_rtp_defaults.sql
-- Change untouched defaults only; preserve settings explicitly saved by Creator.
UPDATE rtp_settings SET target_bps=10000,revision=revision+1
 WHERE mode='LOBBY' AND target_bps=9700 AND revision=0;
UPDATE rtp_settings SET target_bps=9750,revision=revision+1
 WHERE mode='CLUB' AND target_bps=9700 AND revision=0;

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(11,'11','super ace rtp defaults','SQL','V11__super_ace_rtp_defaults.sql',594068105,CURRENT_USER(),0,1);

-- V12__dragon_tiger_timed_rounds.sql
ALTER TABLE round_ledger ADD COLUMN game_round_id BIGINT;
ALTER TABLE lobby_round_ledger ADD COLUMN game_round_id BIGINT;
CREATE UNIQUE INDEX ledger_player_game_round ON round_ledger(player_id,game_type,game_round_id);
CREATE UNIQUE INDEX lobby_ledger_player_game_round ON lobby_round_ledger(player_id,game_type,game_round_id);
CREATE TABLE dragon_tiger_rounds (
 round_id BIGINT PRIMARY KEY,
 starts_at BIGINT NOT NULL,
 betting_closes_at BIGINT NOT NULL,
 reveal_ends_at BIGINT NOT NULL,
 outcome_json LONGTEXT NOT NULL
);
CREATE INDEX dragon_tiger_round_expiry ON dragon_tiger_rounds(reveal_ends_at);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(12,'12','dragon tiger timed rounds','SQL','V12__dragon_tiger_timed_rounds.sql',-1036159462,CURRENT_USER(),0,1);

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
