package vn.emerald.ace;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
class MigrationV12Test {
 @Test void upgradeV11KeepsLedgerAndAddsTimedRoundStorage(){
  var source=new DriverManagerDataSource("jdbc:h2:mem:migration_v12;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
  Flyway.configure().dataSource(source).locations("classpath:db/migration").target("11").load().migrate();var db=new JdbcTemplate(source);
  db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,created_at) VALUES('p','p','P','h','PLAYER',1)");db.update("INSERT INTO wallets(player_id,balance) VALUES('p',12345)");db.update("INSERT INTO lobby_wallets(player_id,balance) VALUES('p',54321)");
  Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
  assertEquals(12345L,db.queryForObject("SELECT balance FROM wallets WHERE player_id='p'",Long.class));assertEquals(54321L,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id='p'",Long.class));assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM dragon_tiger_rounds",Integer.class));
  db.update("INSERT INTO dragon_tiger_rounds(round_id,starts_at,betting_closes_at,reveal_ends_at,outcome_json) VALUES(1,0,10000,15000,'{}')");assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM dragon_tiger_rounds",Integer.class));
 }
}
