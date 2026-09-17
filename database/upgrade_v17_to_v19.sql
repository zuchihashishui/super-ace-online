-- ONLY for schema V17. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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

-- V19__lucky_nine.sql
CREATE TABLE lucky_nine_deals (
 player_id VARCHAR(36) NOT NULL,
 mode VARCHAR(8) NOT NULL,
 request_id VARCHAR(36) NOT NULL,
 deck_json VARCHAR(300) NOT NULL,
 PRIMARY KEY(player_id,mode,request_id),
 FOREIGN KEY(player_id) REFERENCES accounts(id)
);
ALTER TABLE round_ledger ADD COLUMN ln_active BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE lobby_round_ledger ADD COLUMN ln_active BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX ledger_ln_active ON round_ledger(player_id,ln_active);
CREATE INDEX lobby_ln_active ON lobby_round_ledger(player_id,ln_active);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(19,'19','lucky nine','SQL','V19__lucky_nine.sql',1082792979,CURRENT_USER(),0,1);
