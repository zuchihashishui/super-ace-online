"""Generate versioned MySQL install and upgrade files from the actual Flyway migrations.
Run whenever a migration changes. Never edit migrations already released.
"""
import pathlib,zlib
root=pathlib.Path(__file__).resolve().parents[1]
directory=root/'database';directory.mkdir(exist_ok=True)
migrations=sorted((root/'server/src/main/resources/db/migration').glob('V*__*.sql'),key=lambda p:int(p.name.split('__')[0][1:]))
def quote(s):return "'"+s.replace("'","''")+"'"
def history(p):
    version,description=p.stem[1:].split('__',1)
    checksum=0
    for line in p.read_text(encoding='utf-8-sig').splitlines():checksum=zlib.crc32(line.encode(),checksum)
    if checksum>=2**31:checksum-=2**32
    return f"INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES({version},{quote(version)},{quote(description.replace('_',' '))},'SQL',{quote(p.name)},{checksum},CURRENT_USER(),0,1);\n"
header="""-- Super Ace database schema V6 (MySQL 8+)
-- FRESH INSTALL ONLY. Do not import this file into an existing populated database.
-- For existing V4/V5 databases use the matching upgrade file, OR simply start the new server.
-- Default Creator: zuchiha / 112357. Password is stored as a BCrypt hash.
CREATE DATABASE IF NOT EXISTS ace CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ace;
"""
tracker="""CREATE TABLE flyway_schema_history (
 installed_rank INT NOT NULL PRIMARY KEY, version VARCHAR(50), description VARCHAR(200) NOT NULL,
 type VARCHAR(20) NOT NULL, script VARCHAR(1000) NOT NULL, checksum INT,
 installed_by VARCHAR(100) NOT NULL, installed_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 execution_time INT NOT NULL, success BOOLEAN NOT NULL
);
CREATE INDEX flyway_schema_history_s_idx ON flyway_schema_history(success);
"""
seed="""
-- Club and management seed. All four default accounts use password 112357.
INSERT INTO accounts(id,username,display_name,password_hash,role,parent_id,public_code,commission_bps,created_at) VALUES
 ('00000000-0000-0000-0000-000000000001','zuchiha','Creator','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','CREATOR',NULL,'000001',NULL,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)),
 ('00000000-0000-0000-0000-000000000002','zuchiha1','Super Agent','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','SUPER_AGENT','00000000-0000-0000-0000-000000000001','000002',NULL,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)),
 ('00000000-0000-0000-0000-000000000003','zuchiha2','Agent','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','AGENT','00000000-0000-0000-0000-000000000002','000003',0,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)),
 ('00000000-0000-0000-0000-000000000004','zuchiha3','Player','$2a$12$7lviHGXX73Z3wUgK1BVA3eUzskW0aO1AfHi/IxdAh.J8VbZLJdXw2','PLAYER','00000000-0000-0000-0000-000000000003','000004',NULL,ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000));
INSERT INTO wallets(player_id,balance) SELECT id,0 FROM accounts;
INSERT INTO lobby_wallets(player_id,balance) SELECT id,CASE WHEN role='PLAYER' THEN 1000000 ELSE 0 END FROM accounts;
INSERT INTO direct_registration(id,agent_id,super_agent_id) VALUES(1,'00000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000002');
UPDATE clubs SET owner_id='00000000-0000-0000-0000-000000000001',created_at=ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000) WHERE public_code='686868';
"""
(directory/'super_ace.sql').write_text(header+'\n'.join('-- '+p.name+'\n'+p.read_text() for p in migrations)+'\n'+seed+'\n'+tracker+''.join(history(p) for p in migrations))
for start in (4,5):
    selected=[p for p in migrations if int(p.name.split('__')[0][1:])>start]
    (directory/f'upgrade_v{start}_to_v6.sql').write_text(f'-- ONLY for schema V{start}. Stop the server before importing. Back up the database first.\n-- Do not run if Flyway already applied these versions.\nUSE ace;\n'+''.join('\n-- '+p.name+'\n'+p.read_text()+'\n'+history(p) for p in selected))
print('Generated full MySQL install and V4/V5 upgrades at schema V6.')
