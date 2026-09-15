CREATE TABLE direct_registration (
 id INTEGER PRIMARY KEY,
 agent_id VARCHAR(36) NOT NULL,
 super_agent_id VARCHAR(36) NOT NULL,
 FOREIGN KEY(agent_id) REFERENCES accounts(id),
 FOREIGN KEY(super_agent_id) REFERENCES accounts(id)
);
