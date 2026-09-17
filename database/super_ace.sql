-- Super Ace database schema V20 (MySQL 8+)
-- FRESH INSTALL ONLY. Do not import this file into an existing populated database.
-- For existing V4–V19 databases use the matching upgrade file, OR simply start the new server.
-- Default Creator: zuchiha / 112357. Password is stored as a BCrypt hash.
CREATE DATABASE IF NOT EXISTS ace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ace;
-- V1__accounts_ledger_reports.sql
CREATE TABLE accounts (
 id VARCHAR(36) PRIMARY KEY, username VARCHAR(40) NOT NULL UNIQUE, display_name VARCHAR(60) NOT NULL,
 password_hash VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL, parent_id VARCHAR(36), enabled BOOLEAN NOT NULL DEFAULT TRUE,
 commission_bps INTEGER, created_at BIGINT NOT NULL, failed_logins INTEGER NOT NULL DEFAULT 0, locked_until BIGINT NOT NULL DEFAULT 0,
 FOREIGN KEY(parent_id) REFERENCES accounts(id)
);
CREATE INDEX accounts_parent ON accounts(parent_id);
CREATE TABLE wallets (
 player_id VARCHAR(36) PRIMARY KEY, balance BIGINT NOT NULL, free_spins INTEGER NOT NULL DEFAULT 0,
 locked_bet BIGINT NOT NULL DEFAULT 0, revision BIGINT NOT NULL DEFAULT 0, last_spin BIGINT NOT NULL DEFAULT 0,
 FOREIGN KEY(player_id) REFERENCES accounts(id), CHECK(balance>=0), CHECK(free_spins>=0)
);
CREATE TABLE sessions (
 token_hash VARCHAR(64) PRIMARY KEY, account_id VARCHAR(36) NOT NULL, csrf VARCHAR(64) NOT NULL, expires_at BIGINT NOT NULL,
 FOREIGN KEY(account_id) REFERENCES accounts(id)
);
CREATE INDEX sessions_account ON sessions(account_id);
CREATE TABLE app_settings (
 id INTEGER PRIMARY KEY, commission_bps INTEGER NOT NULL, commission_mode VARCHAR(30) NOT NULL, revision BIGINT NOT NULL DEFAULT 0
);
INSERT INTO app_settings(id,commission_bps,commission_mode,revision) VALUES(1,3000,'POSITIVE_PLAYER',0);
CREATE TABLE round_ledger (
 player_id VARCHAR(36) NOT NULL, request_id VARCHAR(36) NOT NULL, agent_id VARCHAR(36) NOT NULL, super_agent_id VARCHAR(36) NOT NULL,
 nominal_bet BIGINT NOT NULL, wager_cents BIGINT NOT NULL, payout_cents BIGINT NOT NULL, is_free BOOLEAN NOT NULL,
 wallet_revision BIGINT NOT NULL, run_id VARCHAR(36), response_json LONGTEXT NOT NULL, created_at BIGINT NOT NULL,
 PRIMARY KEY(player_id,request_id), UNIQUE(player_id,wallet_revision), FOREIGN KEY(player_id) REFERENCES accounts(id)
);
CREATE INDEX ledger_time_player ON round_ledger(created_at,player_id);
CREATE INDEX ledger_agent_time ON round_ledger(agent_id,created_at);
CREATE INDEX ledger_super_time ON round_ledger(super_agent_id,created_at);
CREATE TABLE auto_jobs (
 player_id VARCHAR(36) PRIMARY KEY, run_id VARCHAR(36) NOT NULL UNIQUE, active BOOLEAN NOT NULL, planned INTEGER NOT NULL,
 paid_done INTEGER NOT NULL, free_done INTEGER NOT NULL, bet_cents BIGINT NOT NULL, net_cents BIGINT NOT NULL,
 next_at BIGINT NOT NULL, latest_request_id VARCHAR(36), reason VARCHAR(120) NOT NULL, turbo BOOLEAN NOT NULL, created_at BIGINT NOT NULL,
 FOREIGN KEY(player_id) REFERENCES accounts(id)
);
CREATE INDEX auto_due ON auto_jobs(active,next_at);
CREATE TABLE weekly_settlements (
 agent_id VARCHAR(36) NOT NULL, super_agent_id VARCHAR(36) NOT NULL, week_start VARCHAR(10) NOT NULL,
 wager_cents BIGINT NOT NULL, payout_cents BIGINT NOT NULL, loss_base_cents BIGINT NOT NULL,
 rate_bps INTEGER NOT NULL, commission_mode VARCHAR(30) NOT NULL, commission_cents BIGINT NOT NULL,
 status VARCHAR(16) NOT NULL, revision BIGINT NOT NULL DEFAULT 0, closed_at BIGINT NOT NULL,
 approved_by VARCHAR(36), paid_at BIGINT, note VARCHAR(200), details_json LONGTEXT NOT NULL,
 PRIMARY KEY(agent_id,week_start), FOREIGN KEY(agent_id) REFERENCES accounts(id)
);
CREATE TABLE audit_log (
 id VARCHAR(36) PRIMARY KEY, actor_id VARCHAR(36) NOT NULL, action_name VARCHAR(60) NOT NULL,
 target_id VARCHAR(80) NOT NULL, detail_text VARCHAR(1000) NOT NULL, created_at BIGINT NOT NULL
);
CREATE TABLE chip_transfers (
 id VARCHAR(36) PRIMARY KEY, player_id VARCHAR(36) NOT NULL, request_id VARCHAR(36) NOT NULL,
 kind VARCHAR(16) NOT NULL, amount_cents BIGINT NOT NULL, status VARCHAR(16) NOT NULL,
 reference_text VARCHAR(200) NOT NULL, created_at BIGINT NOT NULL, decided_at BIGINT,
 decided_by VARCHAR(36), UNIQUE(player_id,request_id), FOREIGN KEY(player_id) REFERENCES accounts(id), CHECK(amount_cents>0)
);
CREATE INDEX transfers_player_time ON chip_transfers(player_id,created_at);

-- V2__hierarchy_jwt_rtp.sql
ALTER TABLE accounts ADD public_code VARCHAR(6);
CREATE UNIQUE INDEX accounts_code ON accounts(public_code);
CREATE TABLE hierarchy_lock (id INTEGER PRIMARY KEY);
INSERT INTO hierarchy_lock VALUES(1);
INSERT INTO wallets(player_id,balance,free_spins,locked_bet,revision,last_spin)
 SELECT id,0,0,0,0,0 FROM accounts WHERE id NOT IN (SELECT player_id FROM wallets);
CREATE TABLE refresh_tokens(token_hash VARCHAR(64) PRIMARY KEY,account_id VARCHAR(36) NOT NULL,session_hash VARCHAR(64) NOT NULL,expires_at BIGINT NOT NULL,FOREIGN KEY(account_id) REFERENCES accounts(id));
CREATE INDEX refresh_account ON refresh_tokens(account_id);
CREATE TABLE chip_movements(id VARCHAR(36) PRIMARY KEY,actor_id VARCHAR(36) NOT NULL,target_id VARCHAR(36) NOT NULL,direction VARCHAR(10) NOT NULL,amount_cents BIGINT NOT NULL,created_at BIGINT NOT NULL);
CREATE TABLE rtp_schedule(id INTEGER PRIMARY KEY,starts_at BIGINT NOT NULL);
ALTER TABLE wallets ADD bonus_profile VARCHAR(20);
ALTER TABLE round_ledger ADD rtp_profile VARCHAR(20) NOT NULL DEFAULT 'LEGACY';

-- V3__direct_registration.sql
CREATE TABLE direct_registration (
 id INTEGER PRIMARY KEY,
 agent_id VARCHAR(36) NOT NULL,
 super_agent_id VARCHAR(36) NOT NULL,
 FOREIGN KEY(agent_id) REFERENCES accounts(id),
 FOREIGN KEY(super_agent_id) REFERENCES accounts(id)
);

-- V4__lucky_seven_club.sql
CREATE TABLE clubs (
 id VARCHAR(36) PRIMARY KEY,
 public_code VARCHAR(6) NOT NULL UNIQUE,
 name VARCHAR(80) NOT NULL UNIQUE,
 owner_id VARCHAR(36),
 enabled BOOLEAN NOT NULL DEFAULT TRUE,
 created_at BIGINT NOT NULL
);

INSERT INTO clubs(id,public_code,name,enabled,created_at)
 VALUES('00000000-0000-0000-0000-000000686868','686868','LUCKY SEVEN',TRUE,0);

ALTER TABLE accounts ADD club_id VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000686868';
UPDATE accounts SET club_id='00000000-0000-0000-0000-000000686868' WHERE club_id IS NULL;
ALTER TABLE accounts ADD CONSTRAINT accounts_club_fk FOREIGN KEY(club_id) REFERENCES clubs(id);
CREATE INDEX accounts_club ON accounts(club_id);

ALTER TABLE clubs ADD CONSTRAINT clubs_owner_fk FOREIGN KEY(owner_id) REFERENCES accounts(id);
UPDATE clubs SET owner_id=(SELECT id FROM accounts WHERE role='CREATOR' ORDER BY created_at,id LIMIT 1)
 WHERE public_code='686868';

ALTER TABLE direct_registration ADD club_id VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000686868';
UPDATE direct_registration SET club_id='00000000-0000-0000-0000-000000686868' WHERE club_id IS NULL;
ALTER TABLE direct_registration ADD CONSTRAINT direct_registration_club_fk FOREIGN KEY(club_id) REFERENCES clubs(id);

-- V5__lobby_gold.sql
-- Existing balances and round history remain CLUB chips. New LOBBY Gold is separate.
CREATE TABLE lobby_wallets (
 player_id VARCHAR(36) PRIMARY KEY, balance BIGINT NOT NULL,
 free_spins INTEGER NOT NULL DEFAULT 0, locked_bet BIGINT NOT NULL DEFAULT 0,
 revision BIGINT NOT NULL DEFAULT 0, last_spin BIGINT NOT NULL DEFAULT 0,
 bonus_profile VARCHAR(20),
 FOREIGN KEY(player_id) REFERENCES accounts(id), CHECK(balance>=0), CHECK(free_spins>=0)
);
INSERT INTO lobby_wallets(player_id,balance)
 SELECT id,CASE WHEN role='PLAYER' THEN 1000000 ELSE 0 END FROM accounts;
CREATE TABLE lobby_round_ledger (
 player_id VARCHAR(36) NOT NULL, request_id VARCHAR(36) NOT NULL,
 agent_id VARCHAR(36) NOT NULL, super_agent_id VARCHAR(36) NOT NULL,
 nominal_bet BIGINT NOT NULL, wager_cents BIGINT NOT NULL, payout_cents BIGINT NOT NULL,
 is_free BOOLEAN NOT NULL, wallet_revision BIGINT NOT NULL, run_id VARCHAR(36),
 response_json LONGTEXT NOT NULL, created_at BIGINT NOT NULL, rtp_profile VARCHAR(20) NOT NULL,
 PRIMARY KEY(player_id,request_id), UNIQUE(player_id,wallet_revision),
 FOREIGN KEY(player_id) REFERENCES accounts(id)
);
CREATE INDEX lobby_ledger_time ON lobby_round_ledger(created_at,player_id);
CREATE TABLE lobby_auto_jobs (
 player_id VARCHAR(36) PRIMARY KEY, run_id VARCHAR(36) NOT NULL UNIQUE,
 active BOOLEAN NOT NULL, planned INTEGER NOT NULL, paid_done INTEGER NOT NULL,
 free_done INTEGER NOT NULL, bet_cents BIGINT NOT NULL, net_cents BIGINT NOT NULL,
 next_at BIGINT NOT NULL, latest_request_id VARCHAR(36), reason VARCHAR(120) NOT NULL,
 turbo BOOLEAN NOT NULL, created_at BIGINT NOT NULL,
 FOREIGN KEY(player_id) REFERENCES accounts(id)
);
CREATE INDEX lobby_auto_due ON lobby_auto_jobs(active,next_at);

-- V6__chip_notifications.sql
-- Close the old request workflow and return any chips reserved by pending withdrawals.
UPDATE wallets SET balance=balance+COALESCE((SELECT SUM(t.amount_cents) FROM chip_transfers t
 WHERE t.player_id=wallets.player_id AND t.kind='WITHDRAWAL' AND t.status='PENDING'),0),revision=revision+1
 WHERE player_id IN (SELECT player_id FROM chip_transfers WHERE kind='WITHDRAWAL' AND status='PENDING');
UPDATE chip_transfers SET status='REJECTED',reference_text='Request workflow retired; any reserved chips returned'
 WHERE status='PENDING';
CREATE TABLE chip_notifications (
 id VARCHAR(36) PRIMARY KEY, recipient_id VARCHAR(36) NOT NULL, sender_id VARCHAR(36) NOT NULL,
 amount_cents BIGINT NOT NULL, movement_id VARCHAR(36) NOT NULL,
 created_at BIGINT NOT NULL, read_at BIGINT,
 FOREIGN KEY(recipient_id) REFERENCES accounts(id), FOREIGN KEY(sender_id) REFERENCES accounts(id),
 UNIQUE(recipient_id,movement_id)
);
CREATE INDEX chip_notifications_unread ON chip_notifications(recipient_id,read_at,created_at);

-- V7__agent_join_requests.sql
CREATE TABLE agent_join_requests (
 request_id VARCHAR(36) NOT NULL UNIQUE,
 player_id VARCHAR(36) NOT NULL PRIMARY KEY,
 agent_id VARCHAR(36) NOT NULL,
 previous_parent_id VARCHAR(36),
 status VARCHAR(16) NOT NULL,
 created_at BIGINT NOT NULL,
 decided_at BIGINT,
 FOREIGN KEY (player_id) REFERENCES accounts(id),
 FOREIGN KEY (agent_id) REFERENCES accounts(id)
);
CREATE INDEX agent_join_inbox ON agent_join_requests(agent_id,status);

-- V8__unassigned_club_players.sql
-- Players can belong to their Club without being managed by an Agent.
-- Historical attribution remains unchanged; future unassigned rounds have no Agent.
ALTER TABLE round_ledger MODIFY COLUMN agent_id VARCHAR(36) NULL;
ALTER TABLE round_ledger MODIFY COLUMN super_agent_id VARCHAR(36) NULL;
ALTER TABLE lobby_round_ledger MODIFY COLUMN agent_id VARCHAR(36) NULL;
ALTER TABLE lobby_round_ledger MODIFY COLUMN super_agent_id VARCHAR(36) NULL;

-- V9__creator_rtp_settings.sql
CREATE TABLE rtp_settings (
 mode VARCHAR(10) PRIMARY KEY,
 target_bps INTEGER NOT NULL,
 revision BIGINT NOT NULL DEFAULT 0,
 CHECK(target_bps >= 100 AND target_bps <= 10000)
);
INSERT INTO rtp_settings(mode,target_bps) VALUES('LOBBY',9700),('CLUB',9700);

-- V10__dragon_tiger.sql
ALTER TABLE round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
ALTER TABLE lobby_round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
CREATE INDEX ledger_game_player ON round_ledger(player_id,game_type,wallet_revision);
CREATE INDEX lobby_ledger_game_player ON lobby_round_ledger(player_id,game_type,wallet_revision);

-- V11__super_ace_rtp_defaults.sql
-- Change untouched defaults only; preserve settings explicitly saved by Creator.
UPDATE rtp_settings SET target_bps=10000,revision=revision+1
 WHERE mode='LOBBY' AND target_bps=9700 AND revision=0;
UPDATE rtp_settings SET target_bps=9750,revision=revision+1
 WHERE mode='CLUB' AND target_bps=9700 AND revision=0;

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

-- V14__dragon_tiger_table_totals.sql
-- Current-round totals across all bettors, kept separate for each currency wallet.
CREATE INDEX ledger_dt_table_totals ON round_ledger(game_type,game_round_id);
CREATE INDEX lobby_ledger_dt_table_totals ON lobby_round_ledger(game_type,game_round_id);

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


-- Club and management seed. All four default accounts use password 112357.
INSERT INTO accounts(id,username,display_name,password_hash,role,parent_id,public_code,commission_bps,created_at) VALUES
 ('00000000-0000-0000-0000-000000000001','zuchiha','Creator','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','CREATOR',NULL,'000001',NULL,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)),
 ('00000000-0000-0000-0000-000000000002','zuchiha1','Super Agent','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','SUPER_AGENT','00000000-0000-0000-0000-000000000001','000002',NULL,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)),
 ('00000000-0000-0000-0000-000000000003','zuchiha2','Agent','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','AGENT','00000000-0000-0000-0000-000000000002','000003',0,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)),
 ('00000000-0000-0000-0000-000000000004','zuchiha3','Player','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','PLAYER','00000000-0000-0000-0000-000000000003','000004',NULL,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000));
INSERT INTO wallets(player_id,balance) SELECT id,0 FROM accounts;
INSERT INTO lobby_wallets(player_id,balance) SELECT id,CASE WHEN role='PLAYER' THEN 1000000 ELSE 0 END FROM accounts;
INSERT INTO direct_registration(id,agent_id,super_agent_id) VALUES(1,'00000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000002');
UPDATE clubs SET owner_id='00000000-0000-0000-0000-000000000001',created_at=ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000) WHERE public_code='686868';

CREATE TABLE flyway_schema_history (
 installed_rank INT NOT NULL PRIMARY KEY, version VARCHAR(50), description VARCHAR(200) NOT NULL,
 type VARCHAR(20) NOT NULL, script VARCHAR(1000) NOT NULL, checksum INT,
 installed_by VARCHAR(100) NOT NULL, installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 execution_time INT NOT NULL, success BOOLEAN NOT NULL
);
CREATE INDEX flyway_schema_history_s_idx ON flyway_schema_history(success);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(1,'1','accounts ledger reports','SQL','V1__accounts_ledger_reports.sql',1516337208,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(2,'2','hierarchy jwt rtp','SQL','V2__hierarchy_jwt_rtp.sql',1250339110,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(3,'3','direct registration','SQL','V3__direct_registration.sql',-2032295050,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(4,'4','lucky seven club','SQL','V4__lucky_seven_club.sql',-1826169939,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(5,'5','lobby gold','SQL','V5__lobby_gold.sql',-610100519,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(6,'6','chip notifications','SQL','V6__chip_notifications.sql',-1135577595,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(7,'7','agent join requests','SQL','V7__agent_join_requests.sql',85909202,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(8,'8','unassigned club players','SQL','V8__unassigned_club_players.sql',-101948530,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(9,'9','creator rtp settings','SQL','V9__creator_rtp_settings.sql',135689784,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(10,'10','dragon tiger','SQL','V10__dragon_tiger.sql',1445817963,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(11,'11','super ace rtp defaults','SQL','V11__super_ace_rtp_defaults.sql',594068105,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(12,'12','dragon tiger timed rounds','SQL','V12__dragon_tiger_timed_rounds.sql',-1036159462,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(13,'13','dragon tiger multiple bets','SQL','V13__dragon_tiger_multiple_bets.sql',-2052350647,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(14,'14','dragon tiger table totals','SQL','V14__dragon_tiger_table_totals.sql',-854339490,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(15,'15','daily lobby gold','SQL','V15__daily_lobby_gold.sql',-972351333,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(16,'16','color game','SQL','V16__color_game.sql',-1735832447,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(17,'17','crash','SQL','V17__crash.sql',1426965737,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(18,'18','mines','SQL','V18__mines.sql',1210568882,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(19,'19','lucky nine','SQL','V19__lucky_nine.sql',1082792979,CURRENT_USER(),0,1);
INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(20,'20','color jackpot','SQL','V20__color_jackpot.sql',-1988571374,CURRENT_USER(),0,1);
