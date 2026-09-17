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
