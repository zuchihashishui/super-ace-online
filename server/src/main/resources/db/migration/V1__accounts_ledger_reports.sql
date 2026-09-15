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
