-- ONLY for schema V15. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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

-- V18__mines.sql
CREATE TABLE mines_boards (
 player_id VARCHAR(36) NOT NULL,
 mode VARCHAR(8) NOT NULL,
 request_id VARCHAR(36) NOT NULL,
 mines_json VARCHAR(200) NOT NULL,
 PRIMARY KEY(player_id,mode,request_id),
 FOREIGN KEY(player_id) REFERENCES accounts(id)
);
ALTER TABLE round_ledger ADD COLUMN mines_active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE lobby_round_ledger ADD COLUMN mines_active BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX ledger_mines_active ON round_ledger(player_id,mines_active);
CREATE INDEX lobby_mines_active ON lobby_round_ledger(player_id,mines_active);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(18,'18','mines','SQL','V18__mines.sql',1210568882,CURRENT_USER(),0,1);
