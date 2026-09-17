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
