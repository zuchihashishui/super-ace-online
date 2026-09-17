package philip.emerald.ace.Utils;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static org.junit.jupiter.api.Assertions.*;
class MigrationV17Test {
 @Test void preservesExistingBalancesAndColorRound(){var ds=new DriverManagerDataSource("jdbc:h2:mem:crashmigration;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1","sa","");Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("16").load().migrate();var db=new JdbcTemplate(ds);
 db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,created_at) VALUES('p','p','P','h','PLAYER',1)");db.update("INSERT INTO wallets(player_id,balance) VALUES('p',12345)");db.update("INSERT INTO lobby_wallets(player_id,balance) VALUES('p',54321)");db.update("INSERT INTO color_game_rounds VALUES(1,1,2,3,'{}')");Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();assertEquals(12345L,db.queryForObject("SELECT balance FROM wallets WHERE player_id='p'",Long.class));assertEquals(54321L,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id='p'",Long.class));assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM color_game_rounds",Integer.class));assertEquals(0L,db.queryForObject("SELECT round_id FROM crash_table_state WHERE id=1",Long.class));}
}
