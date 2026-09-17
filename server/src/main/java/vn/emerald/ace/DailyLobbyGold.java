package vn.emerald.ace;
import java.time.*;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** One additive grant per Manila calendar day; independent of login and Club accounting. */
@Service
public class DailyLobbyGold {
 static final long AMOUNT=1_000_000L;
 private final JdbcTemplate db;
 private final TransactionTemplate tx;
 private final boolean enabled;
 public DailyLobbyGold(JdbcTemplate db,PlatformTransactionManager manager,@Value("${ace.jobs-enabled:true}") boolean enabled){this.db=db;this.tx=new TransactionTemplate(manager);this.enabled=enabled;}
 @Scheduled(fixedDelay=60000,initialDelay=5000)
 public void scheduled(){if(enabled)grantAll(LocalDate.now(ZoneId.of("Asia/Manila")));}
 public void grantAll(LocalDate day){
  for(String id:db.queryForList("SELECT player_id FROM lobby_wallets ORDER BY player_id",String.class))grant(id,day);
 }
 void grant(String id,LocalDate day){tx.executeWithoutResult(status->{
  db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id=? FOR UPDATE",Long.class,id);
  if(db.queryForObject("SELECT COUNT(*) FROM lobby_daily_rewards WHERE player_id=? AND reward_date=?",Integer.class,id,java.sql.Date.valueOf(day))>0)return;
  db.update("INSERT INTO lobby_daily_rewards(id,player_id,reward_date,amount_cents,created_at) VALUES(?,?,?,?,?)",UUID.randomUUID().toString(),id,java.sql.Date.valueOf(day),AMOUNT,System.currentTimeMillis());
  db.update("UPDATE lobby_wallets SET balance=balance+?,revision=revision+1 WHERE player_id=?",AMOUNT,id);
 });}
}
