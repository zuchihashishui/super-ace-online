-- ONLY for schema V18. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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

-- V20__color_jackpot.sql
CREATE TABLE color_jackpot_pools (
 mode VARCHAR(8) PRIMARY KEY,
 enabled BOOLEAN NOT NULL,
 balance_cents BIGINT NOT NULL,
 remainder_units INT NOT NULL DEFAULT 0,
 contributed_cents BIGINT NOT NULL DEFAULT 0,
 awarded_cents BIGINT NOT NULL DEFAULT 0,
 revision BIGINT NOT NULL DEFAULT 0,
 CHECK (balance_cents >= 0), CHECK (remainder_units >= 0 AND remainder_units < 100)
);
INSERT INTO color_jackpot_pools(mode,enabled,balance_cents) VALUES('LOBBY',TRUE,3000000),('CLUB',FALSE,0);
CREATE TABLE color_jackpot_rounds (
 mode VARCHAR(8) NOT NULL,
 round_id BIGINT NOT NULL,
 enabled BOOLEAN NOT NULL,
 rules_version INT NOT NULL DEFAULT 1,
 settled BOOLEAN NOT NULL DEFAULT FALSE,
 stake_cents BIGINT NOT NULL DEFAULT 0,
 contribution_cents BIGINT NOT NULL DEFAULT 0,
 pool_before_cents BIGINT NOT NULL DEFAULT 0,
 pool_after_cents BIGINT NOT NULL DEFAULT 0,
 tier VARCHAR(8),
 award_cents BIGINT NOT NULL DEFAULT 0,
 settled_at BIGINT,
 PRIMARY KEY(mode,round_id),
 FOREIGN KEY(mode) REFERENCES color_jackpot_pools(mode)
);
CREATE INDEX color_jackpot_pending ON color_jackpot_rounds(mode,settled,round_id);
CREATE TABLE color_jackpot_bets (
 mode VARCHAR(8) NOT NULL,
 player_id VARCHAR(36) NOT NULL,
 request_id VARCHAR(36) NOT NULL,
 round_id BIGINT NOT NULL,
 stake_cents BIGINT NOT NULL,
 PRIMARY KEY(mode,player_id,request_id),
 FOREIGN KEY(mode,round_id) REFERENCES color_jackpot_rounds(mode,round_id),
 FOREIGN KEY(player_id) REFERENCES accounts(id),
 CHECK(stake_cents > 0)
);
CREATE INDEX color_jackpot_bets_round ON color_jackpot_bets(mode,round_id,player_id);
CREATE TABLE color_jackpot_awards (
 id VARCHAR(36) PRIMARY KEY,
 mode VARCHAR(8) NOT NULL,
 round_id BIGINT NOT NULL,
 player_id VARCHAR(36) NOT NULL,
 stake_cents BIGINT NOT NULL,
 amount_cents BIGINT NOT NULL,
 tier VARCHAR(8) NOT NULL,
 created_at BIGINT NOT NULL,
 read_at BIGINT,
 UNIQUE(mode,round_id,player_id),
 FOREIGN KEY(mode,round_id) REFERENCES color_jackpot_rounds(mode,round_id),
 FOREIGN KEY(player_id) REFERENCES accounts(id),
 CHECK(amount_cents > 0)
);
CREATE INDEX color_jackpot_unread ON color_jackpot_awards(player_id,read_at,created_at);
CREATE TABLE color_jackpot_journal (
 id VARCHAR(36) PRIMARY KEY,
 mode VARCHAR(8) NOT NULL,
 request_id VARCHAR(64) NOT NULL,
 actor_id VARCHAR(36),
 round_id BIGINT,
 kind VARCHAR(16) NOT NULL,
 amount_cents BIGINT NOT NULL,
 balance_after_cents BIGINT NOT NULL,
 created_at BIGINT NOT NULL,
 UNIQUE(mode,request_id),
 FOREIGN KEY(mode) REFERENCES color_jackpot_pools(mode)
);
INSERT INTO color_jackpot_journal(id,mode,request_id,kind,amount_cents,balance_after_cents,created_at)
 VALUES('00000000-0000-0000-0000-000000000020','LOBBY','V20-SYSTEM-SEED','SEED',3000000,3000000,0);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(20,'20','color jackpot','SQL','V20__color_jackpot.sql',-1988571374,CURRENT_USER(),0,1);
