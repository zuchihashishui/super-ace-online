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
