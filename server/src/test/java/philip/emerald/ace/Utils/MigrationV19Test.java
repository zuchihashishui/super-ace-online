package philip.emerald.ace.Utils;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
class MigrationV19Test {
 @Test void luckyNineMigrationPreservesExistingWalletAndSecret(){var ds=new DriverManagerDataSource("jdbc:h2:mem:lucky9migration;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("18").load().migrate();var db=new JdbcTemplate(ds);db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,created_at) VALUES('p','p','P','h','PLAYER',1)");db.update("INSERT INTO wallets(player_id,balance) VALUES('p',123)");db.update("INSERT INTO lobby_wallets(player_id,balance) VALUES('p',456)");db.update("INSERT INTO mines_boards VALUES('p','LOBBY','r','[0,1,2]')");Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();assertEquals(123L,db.queryForObject("SELECT balance FROM wallets WHERE player_id='p'",Long.class));assertEquals(456L,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id='p'",Long.class));assertEquals("[0,1,2]",db.queryForObject("SELECT mines_json FROM mines_boards",String.class));assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM lucky_nine_deals",Integer.class));}
}
