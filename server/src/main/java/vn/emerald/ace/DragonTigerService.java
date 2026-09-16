package vn.emerald.ace;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class DragonTigerService {
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;private final long cooldown;
 private final DragonTigerEngine engine=new DragonTigerEngine();
 public DragonTigerService(GameService club,LobbyGameService lobby,Accounts accounts,@Value("${ace.spin-cooldown-ms}")long cooldown){this.club=club;this.lobby=lobby;this.accounts=accounts;this.cooldown=cooldown;}
 GameService selected(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 public record Round(String requestId,String mode,DragonTigerEngine.Side side,long betCents,long payoutCents,long balanceBeforeCents,long balanceCents,long revision,long createdAt,DragonTigerEngine.Outcome outcome){}
 @Transactional public Round play(Accounts.User actor,String mode,String requestId,DragonTigerEngine.Side side,long bet,long revision){
  var game=selected(mode);accounts.requireGame(actor);var w=game.wallet(actor.id(),true);
  var u=accounts.user(actor.id());if(!u.enabled())throw Accounts.unauthorized();accounts.requireGame(u);
  var prior=game.db().query("SELECT game_type,response_json FROM "+game.table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->{
   if(!r.getString(1).equals("DRAGON_TIGER"))throw GameService.error(409,"REQUEST_REUSED");
   return game.decode(r.getString(2),Round.class);
  },u.id(),requestId);
  if(!prior.isEmpty()){var saved=prior.getFirst();if(saved.betCents()!=bet||saved.side()!=side)throw GameService.error(409,"REQUEST_REUSED");return saved;}
  if(side==null||!GameEngine.validBet(bet))throw GameService.error(400,"INVALID_BET");
  if(game.db().queryForObject("SELECT COUNT(*) FROM "+game.table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,u.id())>0)throw GameService.error(409,"AUTO_ACTIVE");
  if(w.freeSpins()>0)throw GameService.error(409,"BET_LOCKED");
  if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");
  long now=System.currentTimeMillis();if(now-w.lastSpin()<cooldown)throw GameService.error(429,"TOO_FAST");
  if(w.balanceCents()<bet)throw GameService.error(409,"INSUFFICIENT_FUNDS");
  var outcome=engine.deal();long payout=DragonTigerEngine.payout(bet,side,outcome.winner()),balance=Math.addExact(w.balanceCents()-bet,payout);
  var result=new Round(requestId,mode,side,bet,payout,w.balanceCents(),balance,w.revision()+1,now,outcome);
  game.db().update("UPDATE "+game.table("wallets")+" SET balance=?,revision=?,last_spin=? WHERE player_id=?",balance,result.revision(),now,u.id());
  var agent=u.role()==Accounts.Role.AGENT?u:(u.role()==Accounts.Role.PLAYER&&u.parentId()!=null?accounts.user(u.parentId()):null);
  game.db().update("INSERT INTO "+game.table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type) VALUES(?,?,?,?,?,?,?,FALSE,?,?,?,?,?)",
   u.id(),requestId,agent==null?null:agent.id(),u.role()==Accounts.Role.SUPER_AGENT?u.id():(agent==null?null:agent.parentId()),bet,bet,payout,result.revision(),game.encode(result),now,"DT_TWO_CARD_V1","DRAGON_TIGER");
  return result;
 }
 public List<Round> history(Accounts.User actor,String mode){
  accounts.requireGame(actor);var game=selected(mode);
  return game.db().query("SELECT response_json FROM "+game.table("round_ledger")+" WHERE player_id=? AND game_type='DRAGON_TIGER' ORDER BY wallet_revision DESC LIMIT 50",(r,n)->game.decode(r.getString(1),Round.class),actor.id());
 }
}
