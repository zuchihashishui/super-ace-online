-- ONLY for schema V13. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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
