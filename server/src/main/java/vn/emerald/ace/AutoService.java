package vn.emerald.ace;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @org.springframework.context.annotation.Primary
public class AutoService {
 private final GameService game;private final Accounts accounts;
 public AutoService(GameService game,Accounts accounts){this.game=game;this.accounts=accounts;}
 public record Job(String runId,boolean active,int planned,int completed,int freeCompleted,long betCents,long netCents,long nextAt,String latestRequestId,String reason,boolean turbo){}
 public record State(GameService.Me wallet,Job job,GameService.SpinResult latest){}
 Job job(String id){return game.db().query("SELECT * FROM "+game.table("auto_jobs")+" WHERE player_id=?",(r,n)->new Job(r.getString("run_id"),r.getBoolean("active"),r.getInt("planned"),r.getInt("paid_done"),r.getInt("free_done"),r.getLong("bet_cents"),r.getLong("net_cents"),r.getLong("next_at"),r.getString("latest_request_id"),r.getString("reason"),r.getBoolean("turbo")),id).stream().findFirst().orElse(null);}
 @Transactional public State state(Accounts.Auth auth){accounts.requireGame(auth.user());game.wallet(auth.user().id(),true);var j=job(auth.user().id());return new State(game.me(auth),j,j==null||j.latestRequestId()==null?null:game.saved(auth.user().id(),j.latestRequestId()));}
 @Transactional public Job start(Accounts.User u,String runId,int planned,long bet,long revision,boolean turbo){
  accounts.requireGame(u);var w=game.wallet(u.id(),true);var old=job(u.id());if(old!=null&&old.runId().equals(runId)){if(old.planned()!=planned||old.betCents()!=bet||old.turbo()!=turbo)throw GameService.error(409,"REQUEST_REUSED");return old;}
  if(old!=null&&old.active())throw GameService.error(409,"AUTO_ACTIVE");if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");if(!List.of(10,25,50,100,250,500).contains(planned))throw GameService.error(400,"INVALID_REQUEST");if(!GameEngine.validBet(bet))throw GameService.error(400,"INVALID_BET");if(w.freeSpins()>0&&w.lockedBetCents()!=bet)throw GameService.error(409,"BET_LOCKED");if(w.freeSpins()==0&&w.balanceCents()<bet)throw GameService.error(409,"INSUFFICIENT_FUNDS");
  // The wallet lock serializes start/stop/tick/manual spin and transfer approval.
  game.db().update("DELETE FROM "+game.table("auto_jobs")+" WHERE player_id=?",u.id());game.db().update("INSERT INTO "+game.table("auto_jobs")+"(player_id,run_id,active,planned,paid_done,free_done,bet_cents,net_cents,next_at,reason,turbo,created_at) VALUES(?,?,TRUE,?,0,0,?,0,?,'RUNNING',?,?)",u.id(),runId,planned,bet,System.currentTimeMillis(),turbo,System.currentTimeMillis());return job(u.id());
 }
 @Transactional public Job stop(Accounts.User u,String runId){accounts.requireGame(u);game.wallet(u.id(),true);game.db().update("UPDATE "+game.table("auto_jobs")+" SET active=FALSE,reason='STOPPED' WHERE player_id=? AND run_id=?",u.id(),runId);return job(u.id());}
 @Transactional public void process(String id){var w=game.wallet(id,true);var j=job(id);long now=System.currentTimeMillis();if(j==null||!j.active()||j.nextAt()>now)return;var u=accounts.user(id);String reason=null;
  if(!u.enabled()||!Accounts.canPlay(u))reason="ACCOUNT_DISABLED";else if(j.completed()>=j.planned()&&w.freeSpins()==0)reason="COMPLETED";else if(w.freeSpins()==0&&w.balanceCents()<j.betCents())reason="INSUFFICIENT_FUNDS";
  if(reason!=null){game.db().update("UPDATE "+game.table("auto_jobs")+" SET active=FALSE,reason=? WHERE player_id=?",reason,id);return;}
  var result=game.executeLocked(u,w,UUID.randomUUID().toString(),j.betCents(),j.runId());int paid=j.completed()+(result.freeSpin()?0:1),free=j.freeCompleted()+(result.freeSpin()?1:0);boolean active=paid<j.planned()||result.freeSpins()>0;
  long delay=(j.turbo()?1900:3900)+(long)result.outcome().cascades().size()*(j.turbo()?700:1400)+(result.outcome().freeAward()>0?(j.turbo()?900:1800):0);
  game.db().update("UPDATE "+game.table("auto_jobs")+" SET active=?,paid_done=?,free_done=?,net_cents=?,next_at=?,latest_request_id=?,reason=? WHERE player_id=?",active,paid,free,Math.addExact(j.netCents(),result.outcome().winCents()-(result.freeSpin()?0:result.betCents())),now+delay,result.requestId(),active?"RUNNING":"COMPLETED",id);
 }
}
