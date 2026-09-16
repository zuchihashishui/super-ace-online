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
