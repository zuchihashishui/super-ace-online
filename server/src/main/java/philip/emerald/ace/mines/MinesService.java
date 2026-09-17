package philip.emerald.ace.mines;
import philip.emerald.ace.superace.GameEngine;
import philip.emerald.ace.Utils.Accounts;
import philip.emerald.ace.superace.LobbyGameService;
import philip.emerald.ace.superace.GameService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class MinesService {
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;private final MinesEngine engine=new MinesEngine();
 public MinesService(GameService club,LobbyGameService lobby,Accounts accounts){this.club=club;this.lobby=lobby;this.accounts=accounts;}
 GameService game(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 public record Round(String requestId,String mode,long betCents,int mineCount,List<Integer> opened,List<Integer> mines,String status,long multiplierBps,long nextMultiplierBps,long payoutCents,long balanceCents,long revision,long createdAt){}
 Round read(GameService g,String player,String request){var rows=g.db().query("SELECT game_type,response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->{if(!r.getString(1).equals("MINES"))throw GameService.error(409,"REQUEST_REUSED");return g.decode(r.getString(2),Round.class);},player,request);return rows.isEmpty()?null:rows.getFirst();}
 List<Integer> secret(String player,String mode,String request){return Arrays.asList(club.decode(club.db().queryForObject("SELECT mines_json FROM mines_boards WHERE player_id=? AND mode=? AND request_id=?",String.class,player,mode,request),Integer[].class));}
 public List<Round> history(Accounts.User actor,String mode){accounts.requireGame(actor);var g=game(mode);return g.db().query("SELECT response_json FROM "+g.table("round_ledger")+" WHERE player_id=? AND game_type='MINES' ORDER BY created_at DESC,wallet_revision DESC LIMIT 20",(r,n)->g.decode(r.getString(1),Round.class),actor.id());}
 @Transactional public Round start(Accounts.User actor,String mode,String request,long stake,int count,long revision){
  var g=game(mode);accounts.requireGame(actor);var w=g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));var prior=read(g,actor.id(),request);if(prior!=null){if(prior.betCents()!=stake||prior.mineCount()!=count)throw GameService.error(409,"REQUEST_REUSED");return prior;}
  if(!GameEngine.validBet(stake)||!MinesEngine.COUNTS.contains(count))throw GameService.error(400,"INVALID_BET");if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");if(w.freeSpins()>0)throw GameService.error(409,"BET_LOCKED");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,actor.id())>0)throw GameService.error(409,"AUTO_ACTIVE");
  if(g.db().queryForObject("SELECT COUNT(*) FROM "+g.table("round_ledger")+" WHERE player_id=? AND mines_active=TRUE",Integer.class,actor.id())>0)throw GameService.error(409,"ROUND_ACTIVE");if(w.balanceCents()<stake)throw GameService.error(409,"INSUFFICIENT_FUNDS");
  long time=System.currentTimeMillis();var r=new Round(request,mode,stake,count,List.of(),null,"ACTIVE",100,MinesEngine.multiplier(count,1),0,w.balanceCents()-stake,w.revision()+1,time);
  g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",r.balanceCents(),r.revision(),actor.id());g.db().update("INSERT INTO mines_boards(player_id,mode,request_id,mines_json) VALUES(?,?,?,?)",actor.id(),mode,request,g.encode(engine.board(count)));
  var u=accounts.user(actor.id());var agent=u.role()==Accounts.Role.AGENT?u:(u.role()==Accounts.Role.PLAYER&&u.parentId()!=null?accounts.user(u.parentId()):null);
  g.db().update("INSERT INTO "+g.table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type,mines_active) VALUES(?,?,?,?,?,?,0,FALSE,?,?,?,'MINES_V1','MINES',TRUE)",u.id(),request,agent==null?null:agent.id(),u.role()==Accounts.Role.SUPER_AGENT?u.id():(agent==null?null:agent.parentId()),stake,stake,r.revision(),g.encode(r),time);return r;
 }
 @Transactional public Round move(Accounts.User actor,String mode,String request,Integer tile){
  var g=game(mode);accounts.requireGame(actor);var w=g.wallet(actor.id(),true);accounts.requireGame(accounts.user(actor.id()));var old=read(g,actor.id(),request);if(old==null)throw GameService.error(404,"BET_NOT_FOUND");if(!old.status().equals("ACTIVE"))return old;
  var mines=secret(actor.id(),mode,request);var opened=new ArrayList<>(old.opened());String status="ACTIVE";long payout=0;
  if(tile==null){if(opened.isEmpty())throw GameService.error(409,"OPEN_TILE_FIRST");status="CASHED_OUT";}else{if(tile<0||tile>=25)throw GameService.error(400,"INVALID_TILE");if(opened.contains(tile))return old;opened.add(tile);if(mines.contains(tile))status="LOST";else if(opened.size()==25-old.mineCount())status="CASHED_OUT";}
  int safe=(int)opened.stream().filter(v->!mines.contains(v)).count();long multiplier=MinesEngine.multiplier(old.mineCount(),safe);if(status.equals("CASHED_OUT"))payout=MinesEngine.payout(old.betCents(),old.mineCount(),safe);boolean active=status.equals("ACTIVE");
  var row=new Round(request,mode,old.betCents(),old.mineCount(),List.copyOf(opened),active?null:mines,status,multiplier,active?MinesEngine.multiplier(old.mineCount(),safe+1):0,payout,Math.addExact(w.balanceCents(),payout),w.revision()+1,old.createdAt());
  g.db().update("UPDATE "+g.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",row.balanceCents(),row.revision(),actor.id());g.db().update("UPDATE "+g.table("round_ledger")+" SET response_json=?,payout_cents=?,mines_active=? WHERE player_id=? AND request_id=?",g.encode(row),payout,active,actor.id(),request);return row;
 }
}
