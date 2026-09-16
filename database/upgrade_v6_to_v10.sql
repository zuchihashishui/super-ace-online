-- ONLY for schema V6. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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

-- V9__creator_rtp_settings.sql
CREATE TABLE rtp_settings (
 mode VARCHAR(10) PRIMARY KEY,
 target_bps INTEGER NOT NULL,
 revision BIGINT NOT NULL DEFAULT 0,
 CHECK(target_bps >= 100 AND target_bps <= 10000)
);
INSERT INTO rtp_settings(mode,target_bps) VALUES('LOBBY',9700),('CLUB',9700);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(9,'9','creator rtp settings','SQL','V9__creator_rtp_settings.sql',135689784,CURRENT_USER(),0,1);

-- V10__dragon_tiger.sql
ALTER TABLE round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
ALTER TABLE lobby_round_ledger ADD COLUMN game_type VARCHAR(20) NOT NULL DEFAULT 'SUPER_ACE';
CREATE INDEX ledger_game_player ON round_ledger(player_id,game_type,wallet_revision);
CREATE INDEX lobby_ledger_game_player ON lobby_round_ledger(player_id,game_type,wallet_revision);

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(10,'10','dragon tiger','SQL','V10__dragon_tiger.sql',1445817963,CURRENT_USER(),0,1);
