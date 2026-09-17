package philip.emerald.ace.luckynine;
import philip.emerald.ace.Utils.Accounts;
import philip.emerald.ace.superace.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class LuckyNineService {
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;private final LuckyNineEngine engine=new LuckyNineEngine();
 public LuckyNineService(GameService club,LobbyGameService lobby,Accounts accounts){this.club=club;this.lobby=lobby;this.accounts=accounts;}
 GameService game(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 public record Round(String requestId,String mode,long betCents,List<Integer> playerCards,List<Integer> bankerCards,int playerTotal,Integer bankerTotal,String status,String action,long payoutCents,long balanceCents,long revision,long createdAt){}
 Round read(GameService g,String player,String request){var rows=g.db().query("SELECT game_type,response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->{if(!r.getString(1).equals("LUCKY9"))throw GameService.error(409,"REQUEST_REUSED");return g.decode(r.getString(2),Round.class);},player,request);return rows.isEmpty()?null:rows.getFirst();}
 List<Integer> secret(String player,String mode,String request){return Arrays.asList(club.decode(club.db().queryForObject("SELECT deck_json FROM lucky_nine_deals WHERE player_id=? AND mode=? AND request_id=?",String.class,player,mode,request),Integer[].class));}
 public List<Round> history(Accounts.User actor,String mode){accounts.requireGame(actor);var g=game(mode);return g.db().query("SELECT response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND game_type='LUCKY9' ORDER BY created_at DESC,wallet_revision DESC LIMIT 20",(r,n)->g.decode(r.getString(1),Round.class),actor.id());}
 @Transactional public Round start(Accounts.User actor,String mode,String request,long stake,long revision){
  var g=game(mode);accounts.requireGame(actor);var w=g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));var prior=read(g,actor.id(),request);if(prior!=null){if(prior.betCents()!=stake)throw GameService.error(409,"REQUEST_REUSED");return prior;}
  if(!GameEngine.validBet(stake))throw GameService.error(400,"INVALID_BET");if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");if(w.freeSpins()>0)throw GameService.error(409,"BET_LOCKED");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,actor.id())>0)throw GameService.error(409,"AUTO_ACTIVE");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("round_ledger")+" WHERE player_id=? AND ln_active=TRUE",Integer.class,actor.id())>0)throw GameService.error(409,"ROUND_ACTIVE");if(w.balanceCents()<stake)throw GameService.error(409,"INSUFFICIENT_FUNDS");
  var deck=engine.deck();boolean natural=LuckyNineEngine.natural(deck);var outcome=natural?LuckyNineEngine.finish(deck,null):null;long time=System.currentTimeMillis(),paid=natural?LuckyNineEngine.payout(stake,outcome):0;
  var r=new Round(request,mode,stake,natural?outcome.playerCards():LuckyNineEngine.player(deck),natural?outcome.bankerCards():null,natural?outcome.playerTotal():LuckyNineEngine.total(LuckyNineEngine.player(deck)),natural?outcome.bankerTotal():null,natural?outcome.status():"ACTIVE",natural?"NATURAL":null,paid,Math.addExact(w.balanceCents()-stake,paid),w.revision()+1,time);
  g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",r.balanceCents(),r.revision(),actor.id());g.db().update("INSERT INTO lucky_nine_deals(player_id,mode,request_id,deck_json) VALUES(?,?,?,?)",actor.id(),mode,request,g.encode(deck));
  var u=accounts.user(actor.id());var agent=u.role()==Accounts.Role.AGENT?u:(u.role()==Accounts.Role.PLAYER&&u.parentId()!=null?accounts.user(u.parentId()):null);
  g.db().update("INSERT INTO "+g.table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type,ln_active) VALUES(?,?,?,?,?,?,?,FALSE,?,?,?,'LUCKY9_V1','LUCKY9',?)",u.id(),request,agent==null?null:agent.id(),u.role()==Accounts.Role.SUPER_AGENT?u.id():(agent==null?null:agent.parentId()),stake,stake,paid,r.revision(),g.encode(r),time,!natural);return r;
 }
 @Transactional public Round move(Accounts.User actor,String mode,String request,LuckyNineEngine.Action action){
  if(action==null)throw GameService.error(400,"INVALID_ACTION");var g=game(mode);accounts.requireGame(actor);var w=g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));var old=read(g,actor.id(),request);if(old==null)throw GameService.error(404,"BET_NOT_FOUND");if(!old.status().equals("ACTIVE")){if(!"NATURAL".equals(old.action())&&!action.name().equals(old.action()))throw GameService.error(409,"REQUEST_REUSED");return old;}
  var outcome=LuckyNineEngine.finish(secret(actor.id(),mode,request),action);long paid=LuckyNineEngine.payout(old.betCents(),outcome);var row=new Round(request,mode,old.betCents(),outcome.playerCards(),outcome.bankerCards(),outcome.playerTotal(),outcome.bankerTotal(),outcome.status(),action.name(),paid,Math.addExact(w.balanceCents(),paid),w.revision()+1,old.createdAt());
  g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",row.balanceCents(),row.revision(),actor.id());g.db().update("UPDATE "+g.table("round_ledger")+" SET response_json=?,payout_cents=?,ln_active=FALSE WHERE player_id=? AND request_id=?",g.encode(row),paid,actor.id(),request);return row;
 }
}
