package vn.emerald.ace;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;

class MigrationV11Test {
 @Test void defaultsUpgradeOnceAndPreserveCreatorChoices(){
  for(boolean customized:new boolean[]{false,true}){
   var source=new DriverManagerDataSource("jdbc:h2:mem:rtp11_"+customized+";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");
   Flyway.configure().dataSource(source).locations("classpath:db/migration").target("10").load().migrate();
   var db=new JdbcTemplate(source);
   if(customized){db.update("UPDATE rtp_settings SET target_bps=9855,revision=3 WHERE mode='LOBBY'");db.update("UPDATE rtp_settings SET revision=1 WHERE mode='CLUB'");}
   var flyway=Flyway.configure().dataSource(source).locations("classpath:db/migration").load();
   flyway.migrate();
   assertEquals(customized?9855:10000,db.queryForObject("SELECT target_bps FROM rtp_settings WHERE mode='LOBBY'",Integer.class));
   assertEquals(customized?9700:9750,db.queryForObject("SELECT target_bps FROM rtp_settings WHERE mode='CLUB'",Integer.class));
   assertEquals(customized?3L:1L,db.queryForObject("SELECT revision FROM rtp_settings WHERE mode='LOBBY'",Long.class));
   assertEquals(0,flyway.migrate().migrationsExecuted);
   assertEquals(customized?9855:10000,db.queryForObject("SELECT target_bps FROM rtp_settings WHERE mode='LOBBY'",Integer.class));
  }
 }
}
