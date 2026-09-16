package vn.emerald.ace;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;

class MigrationV9Test {
 @Test void upgradeV7PreservesExistingWalletsAndHistoricalAttribution(){
  var source=new DriverManagerDataSource("jdbc:h2:mem:migration_v9;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
  Flyway.configure().dataSource(source).locations("classpath:db/migration").target("7").load().migrate();
  var db=new JdbcTemplate(source);
  db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,parent_id,created_at) VALUES('c','creator','Creator','unchanged-hash','CREATOR',NULL,1),('s','super','Super','hash','SUPER_AGENT','c',1),('a','agent','Agent','hash','AGENT','s',1),('p','player','Player','hash','PLAYER','a',1)");
  db.update("INSERT INTO wallets(player_id,balance) VALUES('p',12345)");
  db.update("INSERT INTO lobby_wallets(player_id,balance) VALUES('p',54321)");
  db.update("INSERT INTO round_ledger(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile) VALUES('p','old','a','s',2000,2000,0,FALSE,1,'{}',1,'CLUB_97')");
  Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
  assertEquals(12345,db.queryForObject("SELECT balance FROM wallets WHERE player_id='p'",Long.class));
  assertEquals(54321,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id='p'",Long.class));
  assertEquals("unchanged-hash",db.queryForObject("SELECT password_hash FROM accounts WHERE id='c'",String.class));
  assertEquals("a",db.queryForObject("SELECT parent_id FROM accounts WHERE id='p'",String.class));
  assertEquals("a",db.queryForObject("SELECT agent_id FROM round_ledger WHERE request_id='old'",String.class));
  assertEquals("CLUB_97",db.queryForObject("SELECT rtp_profile FROM round_ledger WHERE request_id='old'",String.class));
  assertEquals(2,db.queryForObject("SELECT COUNT(*) FROM rtp_settings WHERE target_bps=9700",Integer.class));
  db.update("UPDATE accounts SET parent_id=NULL WHERE id='p'");
  assertEquals("00000000-0000-0000-0000-000000686868",db.queryForObject("SELECT club_id FROM accounts WHERE id='p'",String.class));
  db.update("INSERT INTO round_ledger(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile) VALUES('p','new',NULL,NULL,2000,2000,0,FALSE,2,'{}',2,'RTP_9700')");
 }
}
