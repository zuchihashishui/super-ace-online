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
