-- ONLY for schema V4. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(5,'5','lobby gold','SQL','V5__lobby_gold.sql',-610100519,CURRENT_USER(),0,1);

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

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(6,'6','chip notifications','SQL','V6__chip_notifications.sql',-1135577595,CURRENT_USER(),0,1);

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

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(7,'7','agent join requests','SQL','V7__agent_join_requests.sql',85909202,CURRENT_USER(),0,1);

-- V8__unassigned_club_players.sql
-- Players can belong to their Club without being managed by an Agent.
-- Historical attribution remains unchanged; future unassigned rounds have no Agent.
ALTER TABLE round_ledger MODIFY COLUMN agent_id VARCHAR(36) NULL;
ALTER TABLE round_ledger MODIFY COLUMN super_agent_id VARCHAR(36) NULL;
ALTER TABLE lobby_round_ledger MODIFY COLUMN agent_id VARCHAR(36) NULL;
ALTER TABLE lobby_round_ledger MODIFY COLUMN super_agent_id VARCHAR(36) NULL;

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(8,'8','unassigned club players','SQL','V8__unassigned_club_players.sql',-101948530,CURRENT_USER(),0,1);
