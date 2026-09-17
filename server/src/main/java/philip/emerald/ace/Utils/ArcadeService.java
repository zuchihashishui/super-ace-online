package philip.emerald.ace.Utils;
import philip.emerald.ace.superace.GameEngine;
import philip.emerald.ace.superace.LobbyGameService;
import philip.emerald.ace.superace.GameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class ArcadeService {
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;private final ArcadeEngine engine=new ArcadeEngine();
 public ArcadeService(GameService club,LobbyGameService lobby,Accounts accounts){this.club=club;this.lobby=lobby;this.accounts=accounts;}
 GameService game(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 public record Round(String requestId,String mode,ArcadeEngine.Game game,long betCents,long payoutCents,long balanceCents,long revision,long createdAt,ArcadeEngine.Outcome outcome,int selection){}
 @Transactional public Round play(Accounts.User actor,String mode,ArcadeEngine.Game kind,String request,long stake,long revision){return play(actor,mode,kind,request,stake,revision,0);}
 @Transactional public Round play(Accounts.User actor,String mode,ArcadeEngine.Game kind,String request,long stake,long revision,int selection){
  if(selection<0||selection>19||(kind!=ArcadeEngine.Game.SAKLA&&selection!=0))throw GameService.error(400,"INVALID_SELECTION");
  var g=game(mode);accounts.requireGame(actor);var w=g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));
  var rows=g.db().query("SELECT game_type,response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->{if(!r.getString(1).equals(kind.name()))throw GameService.error(409,"REQUEST_REUSED");return g.decode(r.getString(2),Round.class);},actor.id(),request);
  if(!rows.isEmpty()){var r=rows.getFirst();if(r.betCents()!=stake||r.selection()!=selection)throw GameService.error(409,"REQUEST_REUSED");return r;}
  if(!GameEngine.validBet(stake))throw GameService.error(400,"INVALID_BET");if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");if(w.freeSpins()>0)throw GameService.error(409,"BET_LOCKED");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,actor.id())>0)throw GameService.error(409,"AUTO_ACTIVE");
  if(w.balanceCents()<stake)throw GameService.error(409,"INSUFFICIENT_FUNDS");var outcome=engine.play(kind,selection);long paid=ArcadeEngine.payout(stake,outcome),balance=Math.addExact(w.balanceCents()-stake,paid),time=System.currentTimeMillis();var result=new Round(request,mode,kind,stake,paid,balance,w.revision()+1,time,outcome,selection);
  g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",balance,result.revision(),actor.id());
  var u=accounts.user(actor.id());var agent=u.role()==Accounts.Role.AGENT?u:(u.role()==Accounts.Role.PLAYER&&u.parentId()!=null?accounts.user(u.parentId()):null);
  g.db().update("INSERT INTO "+g.table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type) VALUES(?,?,?,?,?,?,?,FALSE,?,?,?,?,?)",u.id(),request,agent==null?null:agent.id(),u.role()==Accounts.Role.SUPER_AGENT?u.id():(agent==null?null:agent.parentId()),stake,stake,paid,result.revision(),g.encode(result),time,kind.name()+"_V1",kind.name());return result;
 }
 public List<Round> history(Accounts.User actor,String mode,ArcadeEngine.Game kind){accounts.requireGame(actor);var g=game(mode);return g.db().query("SELECT response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND game_type=? ORDER BY wallet_revision DESC LIMIT 20",(r,n)->g.decode(r.getString(1),Round.class),actor.id(),kind.name());}
}
