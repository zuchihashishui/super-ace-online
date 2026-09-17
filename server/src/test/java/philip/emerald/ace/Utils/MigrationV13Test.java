package philip.emerald.ace.Utils;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
class MigrationV13Test {
 @Test void oldRoundsRemainSettledAndMultipleBetsBecomePossible(){
  var source=new DriverManagerDataSource("jdbc:h2:mem:migration_v13;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
  Flyway.configure().dataSource(source).locations("classpath:db/migration").target("12").load().migrate();var db=new JdbcTemplate(source);
  db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,created_at) VALUES('p','p','P','h','PLAYER',1)");
  db.update("INSERT INTO wallets(player_id,balance) VALUES('p',12345)");
  String insert="INSERT INTO round_ledger(player_id,request_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,game_type,game_round_id) VALUES('p',?,500,500,975,FALSE,?,'{}',1,'DRAGON_TIGER',123)";
  db.update(insert,"old",1);
  Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
  assertTrue(db.queryForObject("SELECT dt_settled FROM round_ledger WHERE request_id='old'",Boolean.class));assertEquals(12345L,db.queryForObject("SELECT balance FROM wallets WHERE player_id='p'",Long.class));
  db.update(insert,"new",2);assertEquals(2,db.queryForObject("SELECT COUNT(*) FROM round_ledger WHERE game_round_id=123",Integer.class));
 }
}
