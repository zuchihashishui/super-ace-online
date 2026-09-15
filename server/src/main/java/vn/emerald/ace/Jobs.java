package vn.emerald.ace;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
@Component @ConditionalOnProperty(name="ace.jobs-enabled",havingValue="true",matchIfMissing=true)
public class Jobs {
 @org.springframework.beans.factory.annotation.Autowired LobbyAutoService lobbyAuto;
 private final AutoService auto;private final Reports reports;private final GameService game;
 public Jobs(AutoService auto,Reports reports,GameService game){this.auto=auto;this.reports=reports;this.game=game;}
 @Scheduled(fixedDelay=150,initialDelay=5000) public void spin(){var ids=game.db().query("SELECT player_id FROM auto_jobs WHERE active=TRUE AND next_at<=? ORDER BY next_at LIMIT 100",(r,n)->r.getString(1),System.currentTimeMillis());for(String id:ids)try{auto.process(id);}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Autoplay transaction failed for {}",id,e);}}
 @Scheduled(fixedDelay=150,initialDelay=5000) public void lobbySpin(){var ids=game.db().queryForList("SELECT player_id FROM lobby_auto_jobs WHERE active=TRUE AND next_at<=? ORDER BY next_at LIMIT 100",String.class,System.currentTimeMillis());for(String id:ids)try{lobbyAuto.process(id);}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Lobby autoplay failed for {}",id,e);}}
 @Scheduled(cron="0 0 6 * * MON",zone="${ace.zone}") public void monday(){close();}
 @Scheduled(fixedDelay=3600000,initialDelay=8000) public void close(){for(var week:reports.missingWeeks())reports.closeWeek(week);game.db().update("DELETE FROM refresh_tokens WHERE expires_at<?",System.currentTimeMillis());game.db().update("DELETE FROM sessions WHERE expires_at<?",System.currentTimeMillis());}
}
