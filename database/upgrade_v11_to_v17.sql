-- ONLY for schema V11. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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

-- V14__dragon_tiger_table_totals.sql
-- Current-round totals across all bettors, kept separate for each currency wallet.
CREATE INDEX ledger_dt_table_totals ON round_ledger(game_type,game_round_id);
CREATE INDEX lobby_ledger_dt_table_totals ON lobby_round_ledger(game_type,game_round_id);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(14,'14','dragon tiger table totals','SQL','V14__dragon_tiger_table_totals.sql',-854339490,CURRENT_USER(),0,1);

-- V15__daily_lobby_gold.sql
CREATE TABLE lobby_daily_rewards (
 id VARCHAR(36) PRIMARY KEY,
 player_id VARCHAR(36) NOT NULL,
 reward_date DATE NOT NULL,
 amount_cents BIGINT NOT NULL,
 created_at BIGINT NOT NULL,
 read_at BIGINT,
 FOREIGN KEY (player_id) REFERENCES accounts(id),
 UNIQUE (player_id,reward_date)
);
CREATE INDEX lobby_daily_rewards_unread ON lobby_daily_rewards(player_id,read_at,created_at);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(15,'15','daily lobby gold','SQL','V15__daily_lobby_gold.sql',-972351333,CURRENT_USER(),0,1);

-- V16__color_game.sql
CREATE TABLE color_game_rounds (
 round_id BIGINT PRIMARY KEY,
 starts_at BIGINT NOT NULL,
 betting_closes_at BIGINT NOT NULL,
 reveal_ends_at BIGINT NOT NULL,
 outcome_json LONGTEXT NOT NULL
);
CREATE INDEX color_game_round_expiry ON color_game_rounds(reveal_ends_at);
ALTER TABLE round_ledger ADD COLUMN cg_settled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE lobby_round_ledger ADD COLUMN cg_settled BOOLEAN NOT NULL DEFAULT TRUE;
CREATE INDEX ledger_color_pending ON round_ledger(cg_settled,game_round_id);
CREATE INDEX lobby_color_pending ON lobby_round_ledger(cg_settled,game_round_id);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(16,'16','color game','SQL','V16__color_game.sql',-1735832447,CURRENT_USER(),0,1);

-- V17__crash.sql
CREATE TABLE crash_rounds (
 round_id BIGINT PRIMARY KEY,
 starts_at BIGINT NOT NULL,
 flight_at BIGINT NOT NULL,
 crash_at BIGINT NOT NULL,
 next_at BIGINT NOT NULL,
 crash_bps INT NOT NULL
);
CREATE TABLE crash_table_state (id INT PRIMARY KEY, round_id BIGINT NOT NULL);
INSERT INTO crash_table_state(id,round_id) VALUES(1,0);
ALTER TABLE round_ledger ADD COLUMN crash_due_at BIGINT DEFAULT NULL;
ALTER TABLE lobby_round_ledger ADD COLUMN crash_due_at BIGINT DEFAULT NULL;
CREATE INDEX ledger_crash_due ON round_ledger(crash_due_at);
CREATE INDEX lobby_crash_due ON lobby_round_ledger(crash_due_at);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(17,'17','crash','SQL','V17__crash.sql',1426965737,CURRENT_USER(),0,1);
