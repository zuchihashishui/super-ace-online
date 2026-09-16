-- Change untouched defaults only; preserve settings explicitly saved by Creator.
UPDATE rtp_settings SET target_bps=10000,revision=revision+1
 WHERE mode='LOBBY' AND target_bps=9700 AND revision=0;
UPDATE rtp_settings SET target_bps=9750,revision=revision+1
 WHERE mode='CLUB' AND target_bps=9700 AND revision=0;
