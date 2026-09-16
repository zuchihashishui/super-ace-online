-- ONLY for schema V7. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

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
