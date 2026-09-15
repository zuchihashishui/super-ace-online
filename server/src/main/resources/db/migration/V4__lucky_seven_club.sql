CREATE TABLE clubs (
 id VARCHAR(36) PRIMARY KEY,
 public_code VARCHAR(6) NOT NULL UNIQUE,
 name VARCHAR(80) NOT NULL UNIQUE,
 owner_id VARCHAR(36),
 enabled BOOLEAN NOT NULL DEFAULT TRUE,
 created_at BIGINT NOT NULL
);

INSERT INTO clubs(id,public_code,name,enabled,created_at)
 VALUES('00000000-0000-0000-0000-000000686868','686868','LUCKY SEVEN',TRUE,0);

ALTER TABLE accounts ADD club_id VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000686868';
UPDATE accounts SET club_id='00000000-0000-0000-0000-000000686868' WHERE club_id IS NULL;
ALTER TABLE accounts ADD CONSTRAINT accounts_club_fk FOREIGN KEY(club_id) REFERENCES clubs(id);
CREATE INDEX accounts_club ON accounts(club_id);

ALTER TABLE clubs ADD CONSTRAINT clubs_owner_fk FOREIGN KEY(owner_id) REFERENCES accounts(id);
UPDATE clubs SET owner_id=(SELECT id FROM accounts WHERE role='CREATOR' ORDER BY created_at,id LIMIT 1)
 WHERE public_code='686868';

ALTER TABLE direct_registration ADD club_id VARCHAR(36) DEFAULT '00000000-0000-0000-0000-000000686868';
UPDATE direct_registration SET club_id='00000000-0000-0000-0000-000000686868' WHERE club_id IS NULL;
ALTER TABLE direct_registration ADD CONSTRAINT direct_registration_club_fk FOREIGN KEY(club_id) REFERENCES clubs(id);
