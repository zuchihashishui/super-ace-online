package philip.emerald.ace.Utils;
import philip.emerald.ace.crash.CrashService;
import philip.emerald.ace.colorgame.ColorGameService;
import philip.emerald.ace.superace.LobbyAutoService;
import philip.emerald.ace.superace.AutoService;
import philip.emerald.ace.dragontiger.DragonTigerService;
import philip.emerald.ace.superace.GameService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
@Component @ConditionalOnProperty(name="ace.jobs-enabled",havingValue="true",matchIfMissing=true)
public class Jobs {
 @org.springframework.beans.factory.annotation.Autowired LobbyAutoService lobbyAuto;
 @org.springframework.beans.factory.annotation.Autowired DragonTigerService dragonTiger;
 @Scheduled(fixedDelay=250,initialDelay=1000) public void dragonTiger(){for(String mode:java.util.List.of("LOBBY","CLUB"))for(String id:dragonTiger.pendingPlayers(mode))try{dragonTiger.settle(mode,id,System.currentTimeMillis());}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Dragon Tiger settlement failed for {} {}",mode,id,e);}}
 @org.springframework.beans.factory.annotation.Autowired philip.emerald.ace.colorgame.ColorJackpotService jackpot;
 @Scheduled(fixedDelay=500,initialDelay=1500) public void colorJackpot(){for(String mode:java.util.List.of("LOBBY","CLUB"))try{jackpot.settleDue(mode,System.currentTimeMillis());}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Color Jackpot settlement failed for {}",mode,e);}}
 @org.springframework.beans.factory.annotation.Autowired ColorGameService colorGame;
 @Scheduled(fixedDelay=250,initialDelay=1000) public void colorGame(){for(String mode:java.util.List.of("LOBBY","CLUB"))for(String id:colorGame.pendingPlayers(mode))try{colorGame.settle(mode,id,System.currentTimeMillis());}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Color Game settlement failed for {} {}",mode,id,e);}}
 @org.springframework.beans.factory.annotation.Autowired CrashService crash;
 @Scheduled(fixedDelay=250,initialDelay=1000) public void crash(){for(String mode:java.util.List.of("LOBBY","CLUB"))for(String id:crash.pending(mode))try{crash.settle(mode,id,System.currentTimeMillis());}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Crash settlement failed for {} {}",mode,id,e);}}
 private final AutoService auto;private final Reports reports;private final GameService game;
 public Jobs(AutoService auto,Reports reports,GameService game){this.auto=auto;this.reports=reports;this.game=game;}
 @Scheduled(fixedDelay=150,initialDelay=5000) public void spin(){var ids=game.db().query("SELECT player_id FROM auto_jobs WHERE active=TRUE AND next_at<=? ORDER BY next_at LIMIT 100",(r,n)->r.getString(1),System.currentTimeMillis());for(String id:ids)try{auto.process(id);}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Autoplay transaction failed for {}",id,e);}}
 @Scheduled(fixedDelay=150,initialDelay=5000) public void lobbySpin(){var ids=game.db().queryForList("SELECT player_id FROM lobby_auto_jobs WHERE active=TRUE AND next_at<=? ORDER BY next_at LIMIT 100",String.class,System.currentTimeMillis());for(String id:ids)try{lobbyAuto.process(id);}catch(Exception e){LoggerFactory.getLogger(Jobs.class).error("Lobby autoplay failed for {}",id,e);}}
 @Scheduled(cron="0 0 6 * * MON",zone="${ace.zone}") public void monday(){close();}
 @Scheduled(fixedDelay=3600000,initialDelay=8000) public void close(){for(var week:reports.missingWeeks())reports.closeWeek(week);game.db().update("DELETE FROM refresh_tokens WHERE expires_at<?",System.currentTimeMillis());game.db().update("DELETE FROM sessions WHERE expires_at<?",System.currentTimeMillis());}
}
