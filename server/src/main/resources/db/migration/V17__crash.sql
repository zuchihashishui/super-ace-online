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
