-- ONLY for schema V10. Stop the server before importing. Back up the database first.
-- Do not run if Flyway already applied these versions.
USE ace;

-- V11__super_ace_rtp_defaults.sql
-- Change untouched defaults only; preserve settings explicitly saved by Creator.
UPDATE rtp_settings SET target_bps=10000,revision=revision+1
 WHERE mode='LOBBY' AND target_bps=9700 AND revision=0;
UPDATE rtp_settings SET target_bps=9750,revision=revision+1
 WHERE mode='CLUB' AND target_bps=9700 AND revision=0;

INSERT INTO flyway_schema_history(installed_rank,version,description,type,script,checksum,installed_by,execution_time,success) VALUES(11,'11','super ace rtp defaults','SQL','V11__super_ace_rtp_defaults.sql',594068105,CURRENT_USER(),0,1);
