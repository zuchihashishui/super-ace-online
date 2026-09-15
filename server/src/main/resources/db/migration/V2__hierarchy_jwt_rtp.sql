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
