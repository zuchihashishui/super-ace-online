package philip.emerald.ace.crash;
import philip.emerald.ace.superace.GameEngine;
import philip.emerald.ace.Utils.Accounts;
import philip.emerald.ace.superace.LobbyGameService;
import philip.emerald.ace.superace.GameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class CrashService {
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;private final CrashEngine engine=new CrashEngine();
 public CrashService(GameService club,LobbyGameService lobby,Accounts accounts){this.club=club;this.lobby=lobby;this.accounts=accounts;}
 GameService game(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 long now(){return System.currentTimeMillis();}
 public record Secret(long id,long start,long flight,long crash,long next,int point){}
 public Secret round(long id){return club.db().queryForObject("SELECT * FROM crash_rounds WHERE round_id=?",(r,n)->new Secret(r.getLong("round_id"),r.getLong("starts_at"),r.getLong("flight_at"),r.getLong("crash_at"),r.getLong("next_at"),r.getInt("crash_bps")),id);}
 Secret current(long time){
  long id=club.db().queryForObject("SELECT round_id FROM crash_table_state WHERE id=1 FOR UPDATE",Long.class);
  Secret row=id==0?null:round(id);if(row!=null&&time<row.next())return row;
  int point=engine.sample();long flight=time+CrashEngine.BETTING_MS,crash=CrashEngine.at(flight,point);id++;
  club.db().update("INSERT INTO crash_rounds(round_id,starts_at,flight_at,crash_at,next_at,crash_bps) VALUES(?,?,?,?,?,?)",id,time,flight,crash,crash+CrashEngine.REVEAL_MS,point);
  club.db().update("UPDATE crash_table_state SET round_id=? WHERE id=1",id);return round(id);
 }
 public record Table(long roundId,String phase,long serverTime,long flightAt,Long nextRoundAt,int multiplierBps,Integer crashBps,int bettors,long wagerCents,long paidCents){}
 @Transactional public Table table(String mode){var g=game(mode);long time=now();var row=current(time);String phase=time<row.flight()?"BETTING":time<row.crash()?"FLYING":"CRASHED";
  var sum=g.db().queryForMap("SELECT COUNT(*) n,COALESCE(SUM(wager_cents),0) wager,COALESCE(SUM(payout_cents),0) paid FROM "+g.table("round_ledger")+" WHERE game_type='CRASH' AND game_round_id=?",row.id());
  return new Table(row.id(),phase,time,row.flight(),phase.equals("CRASHED")?row.next():null,phase.equals("BETTING")?100:phase.equals("CRASHED")?row.point():CrashEngine.multiplier(row.flight(),time),phase.equals("CRASHED")?row.point():null,((Number)sum.get("n")).intValue(),((Number)sum.get("wager")).longValue(),((Number)sum.get("paid")).longValue());
 }
 public record Bet(String requestId,String mode,long tableRoundId,long betCents,int autoCashoutBps,String status,int cashoutBps,long payoutCents,long balanceCents,long revision,long createdAt){}
 Bet read(GameService g,String player,String request){var rows=g.db().query("SELECT game_type,response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->{if(!r.getString(1).equals("CRASH"))throw GameService.error(409,"REQUEST_REUSED");return g.decode(r.getString(2),Bet.class);},player,request);return rows.isEmpty()?null:rows.getFirst();}
 void save(GameService g,String player,Bet b){g.db().update("UPDATE "+g.table("round_ledger")+" SET response_json=?,payout_cents=?,crash_due_at=NULL WHERE player_id=? AND request_id=?",g.encode(b),b.payoutCents(),player,b.requestId());g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",b.balanceCents(),b.revision(),player);}
 @Transactional public Bet bet(Accounts.User actor,String mode,long roundId,String request,long stake,int auto,long revision){
  var g=game(mode);accounts.requireGame(actor);settle(mode,actor.id(),now());var w=g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));
  var prior=read(g,actor.id(),request);if(prior!=null){if(prior.tableRoundId()!=roundId||prior.betCents()!=stake||prior.autoCashoutBps()!=auto)throw GameService.error(409,"REQUEST_REUSED");return prior;}
  if(!GameEngine.validBet(stake)||!CrashEngine.validAuto(auto))throw GameService.error(400,"INVALID_BET");
  var row=current(now());long time=now();if(roundId!=row.id()||time>=row.flight())throw GameService.error(409,"BETTING_CLOSED");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("round_ledger")+" WHERE player_id=? AND game_type='CRASH' AND game_round_id=?",Integer.class,actor.id(),roundId)>0)throw GameService.error(409,"BET_ALREADY_PLACED");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,actor.id())>0)throw GameService.error(409,"AUTO_ACTIVE");
  if(w.freeSpins()>0)throw GameService.error(409,"BET_LOCKED");if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");if(w.balanceCents()<stake)throw GameService.error(409,"INSUFFICIENT_FUNDS");
  long due=auto==0?row.crash():Math.min(row.crash(),CrashEngine.at(row.flight(),auto));
  var b=new Bet(request,mode,roundId,stake,auto,"ACTIVE",0,0,w.balanceCents()-stake,w.revision()+1,time);
  g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",b.balanceCents(),b.revision(),actor.id());
  var u=accounts.user(actor.id());var agent=u.role()==Accounts.Role.AGENT?u:(u.role()==Accounts.Role.PLAYER&&u.parentId()!=null?accounts.user(u.parentId()):null);
  g.db().update("INSERT INTO "+g.table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type,game_round_id,crash_due_at) VALUES(?,?,?,?,?,?,0,FALSE,?,?,?,?,?,?,?)",u.id(),request,agent==null?null:agent.id(),u.role()==Accounts.Role.SUPER_AGENT?u.id():(agent==null?null:agent.parentId()),stake,stake,b.revision(),g.encode(b),time,"CRASH_V1","CRASH",roundId,due);return b;
 }
 @Transactional public void settle(String mode,String player,long time){var g=game(mode);var w=g.wallet(player,true);long balance=w.balanceCents(),revision=w.revision();
  var rows=g.db().query("SELECT response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND game_type='CRASH' AND crash_due_at<=? ORDER BY wallet_revision",(r,n)->g.decode(r.getString(1),Bet.class),player,time);
  for(var b:rows){var row=round(b.tableRoundId());boolean won=b.autoCashoutBps()>0&&CrashEngine.at(row.flight(),b.autoCashoutBps())<row.crash();long paid=won?CrashEngine.payout(b.betCents(),b.autoCashoutBps()):0;balance=Math.addExact(balance,paid);revision++;
   save(g,player,new Bet(b.requestId(),mode,b.tableRoundId(),b.betCents(),b.autoCashoutBps(),won?"CASHED_OUT":"LOST",won?b.autoCashoutBps():0,paid,balance,revision,b.createdAt()));}
 }
 @Transactional public Bet cashout(Accounts.User actor,String mode,String request){var g=game(mode);accounts.requireGame(actor);g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));long time=now();settle(mode,actor.id(),time);var b=read(g,actor.id(),request);if(b==null)throw GameService.error(404,"BET_NOT_FOUND");if(!b.status().equals("ACTIVE"))return b;
  var row=round(b.tableRoundId());if(time<row.flight())throw GameService.error(409,"NOT_FLYING");var w=g.wallet(actor.id(),true);int point=CrashEngine.multiplier(row.flight(),time);long paid=CrashEngine.payout(b.betCents(),point);
  var result=new Bet(b.requestId(),mode,b.tableRoundId(),b.betCents(),b.autoCashoutBps(),"CASHED_OUT",point,paid,Math.addExact(w.balanceCents(),paid),w.revision()+1,b.createdAt());save(g,actor.id(),result);return result;
 }
 @Transactional public List<Bet> bets(Accounts.User actor,String mode){accounts.requireGame(actor);settle(mode,actor.id(),now());var g=game(mode);return g.db().query("SELECT response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND game_type='CRASH' ORDER BY created_at DESC LIMIT 20",(r,n)->g.decode(r.getString(1),Bet.class),actor.id());}
 public record Result(long roundId,int crashBps){}
 public List<Result> history(){return club.db().query("SELECT round_id,crash_bps FROM crash_rounds WHERE crash_at<=? ORDER BY round_id DESC LIMIT 20",(r,n)->new Result(r.getLong(1),r.getInt(2)),now());}
 public List<String> pending(String mode){var g=game(mode);return g.db().queryForList("SELECT DISTINCT player_id FROM "+g.table("round_ledger")+" WHERE crash_due_at<=? LIMIT 100",String.class,now());}
}
