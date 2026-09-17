package philip.emerald.ace.Utils;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
class MigrationV20Test {
 @Test void upgradePreservesWalletsAndSeedsPoolOnlyOnce(){var ds=new DriverManagerDataSource("jdbc:h2:mem:jackpotmigration;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("19").load().migrate();var db=new JdbcTemplate(ds);db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,created_at) VALUES('p','p','P','h','PLAYER',1)");db.update("INSERT INTO wallets(player_id,balance) VALUES('p',123)");db.update("INSERT INTO lobby_wallets(player_id,balance) VALUES('p',456)");var flyway=Flyway.configure().dataSource(ds).locations("classpath:db/migration").load();flyway.migrate();flyway.migrate();assertEquals(123L,db.queryForObject("SELECT balance FROM wallets WHERE player_id='p'",Long.class));assertEquals(456L,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id='p'",Long.class));assertEquals(3000000L,db.queryForObject("SELECT balance_cents FROM color_jackpot_pools WHERE mode='LOBBY'",Long.class));assertEquals(0L,db.queryForObject("SELECT balance_cents FROM color_jackpot_pools WHERE mode='CLUB'",Long.class));assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM color_jackpot_journal",Integer.class));assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM color_jackpot_bets",Integer.class));}
}
