package vn.emerald.ace;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class DragonTigerService {
 public static final long BETTING_MS=10_000,REVEAL_MS=5_000,CYCLE_MS=BETTING_MS+REVEAL_MS;
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;
 private final DragonTigerEngine engine=new DragonTigerEngine();
 public DragonTigerService(GameService club,LobbyGameService lobby,Accounts accounts){this.club=club;this.lobby=lobby;this.accounts=accounts;}
 GameService selected(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 long now(){return System.currentTimeMillis();}
 public record Table(long roundId,String phase,long serverTime,long bettingClosesAt,long nextRoundAt,int secondsRemaining,DragonTigerEngine.Outcome outcome){}
 public record TableResult(long tableRoundId,long createdAt,DragonTigerEngine.Outcome outcome){}
 public List<TableResult> tableHistory(){return club.db().query("SELECT round_id,betting_closes_at,outcome_json FROM dragon_tiger_rounds WHERE betting_closes_at<=? ORDER BY round_id DESC LIMIT 20",(r,n)->new TableResult(r.getLong(1),r.getLong(2),club.decode(r.getString(3),DragonTigerEngine.Outcome.class)),now());}
 public record Round(String requestId,String mode,long tableRoundId,long revealAt,DragonTigerEngine.Side side,long betCents,long payoutCents,long balanceBeforeCents,long balanceCents,long revision,long createdAt,DragonTigerEngine.Outcome outcome){}
 public record SidePool(DragonTigerEngine.Side side,long ownCents,long ownChipCount,long othersCents,long othersChipCount,int otherBettors){}
 public record Crowd(long tableRoundId,String mode,long serverTime,int bettors,List<SidePool> sides){}
 public Crowd crowd(Accounts.User viewer,String mode,long id){
  var game=selected(mode);long time=now(),current=roundId(time);if(id<current-1||id>current)throw GameService.error(400,"INVALID_ROUND");
  if(viewer!=null)accounts.requireGame(viewer);
  var sums=new EnumMap<DragonTigerEngine.Side,long[]>(DragonTigerEngine.Side.class);
  var others=new EnumMap<DragonTigerEngine.Side,Set<String>>(DragonTigerEngine.Side.class);var bettors=new HashSet<String>();
  for(var side:DragonTigerEngine.Side.values()){sums.put(side,new long[4]);others.put(side,new HashSet<>());}
  // Only aggregate confirmed stakes. No names, account IDs, cards or payouts leave this method.
  game.db().query("SELECT player_id,response_json FROM "+game.table("round_ledger")+" WHERE game_type='DRAGON_TIGER' AND game_round_id=?",(org.springframework.jdbc.core.RowCallbackHandler)rs->{
   String playerId=rs.getString(1);var bet=game.decode(rs.getString(2),Round.class);var total=sums.get(bet.side());bettors.add(playerId);
   if(viewer!=null&&viewer.id().equals(playerId)){total[0]=Math.addExact(total[0],bet.betCents());total[1]++;}
   else{total[2]=Math.addExact(total[2],bet.betCents());total[3]++;others.get(bet.side()).add(playerId);}
  },id);
  var sides=new ArrayList<SidePool>();for(var side:DragonTigerEngine.Side.values()){var s=sums.get(side);sides.add(new SidePool(side,s[0],s[1],s[2],s[3],others.get(side).size()));}
  return new Crowd(id,mode,time,bettors.size(),sides);
 }
 long roundId(long time){return Math.floorDiv(time,CYCLE_MS);}
 long startsAt(long id){return Math.multiplyExact(id,CYCLE_MS);}
 DragonTigerEngine.Outcome shared(long id){
  var rows=club.db().queryForList("SELECT outcome_json FROM dragon_tiger_rounds WHERE round_id=?",String.class,id);
  if(!rows.isEmpty())return club.decode(rows.getFirst(),DragonTigerEngine.Outcome.class);
  boolean legacy=false;
  for(var game:List.of(club,lobby))if(game.db().queryForObject("SELECT COUNT(*) FROM "+game.table("round_ledger")+" WHERE game_type='DRAGON_TIGER' AND game_round_id=? AND rtp_profile='DT_TWO_CARD_V2'",Integer.class,id)>0)legacy=true;
  var outcome=legacy?engine.dealLegacy():engine.deal();
  try{club.db().update("INSERT INTO dragon_tiger_rounds(round_id,starts_at,betting_closes_at,reveal_ends_at,outcome_json) VALUES(?,?,?,?,?)",id,startsAt(id),startsAt(id)+BETTING_MS,startsAt(id)+CYCLE_MS,club.encode(outcome));return outcome;}
  catch(DuplicateKeyException ignored){return club.decode(club.db().queryForObject("SELECT outcome_json FROM dragon_tiger_rounds WHERE round_id=? FOR UPDATE",String.class,id),DragonTigerEngine.Outcome.class);}
 }
 public Table table(){return tableAt(now());}
 Table tableAt(long time){long id=roundId(time),start=startsAt(id),close=start+BETTING_MS,next=start+CYCLE_MS;boolean reveal=time>=close;return new Table(id,reveal?"REVEAL":"BETTING",time,close,next,(int)Math.max(0,Math.ceil((double)((reveal?next:close)-time)/1000)),reveal?shared(id):null);}
 Round visible(Round round,long time){return time>=round.revealAt()?round:new Round(round.requestId(),round.mode(),round.tableRoundId(),round.revealAt(),round.side(),round.betCents(),round.payoutCents(),round.balanceBeforeCents(),round.balanceCents(),round.revision(),round.createdAt(),null);}
 @Transactional public Round play(Accounts.User actor,String mode,long tableRoundId,String requestId,DragonTigerEngine.Side side,long bet,long revision){
  var game=selected(mode);accounts.requireGame(actor);settle(mode,actor.id(),now());var w=game.wallet(actor.id(),true);long time=now();
  var u=accounts.user(actor.id());if(!u.enabled())throw Accounts.unauthorized();accounts.requireGame(u);
  var prior=game.db().query("SELECT game_type,response_json FROM "+game.table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->{
   if(!r.getString(1).equals("DRAGON_TIGER"))throw GameService.error(409,"REQUEST_REUSED");return game.decode(r.getString(2),Round.class);
  },u.id(),requestId);
  if(!prior.isEmpty()){var saved=prior.getFirst();if(saved.betCents()!=bet||saved.side()!=side||saved.tableRoundId()!=tableRoundId)throw GameService.error(409,"REQUEST_REUSED");return visible(saved,time);}
  long current=roundId(time),close=startsAt(current)+BETTING_MS;
  if(tableRoundId!=current||time>=close)throw GameService.error(409,"BETTING_CLOSED");
  if(side==null||!GameEngine.validBet(bet))throw GameService.error(400,"INVALID_BET");
  if(game.db().queryForObject("SELECT COUNT(*) FROM "+game.table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,u.id())>0)throw GameService.error(409,"AUTO_ACTIVE");
  if(w.freeSpins()>0)throw GameService.error(409,"BET_LOCKED");if(w.revision()!=revision)throw GameService.error(409,"STALE_STATE");
  if(w.balanceCents()<bet)throw GameService.error(409,"INSUFFICIENT_FUNDS");
  // Persist the shared rules/outcome at first accepted bet; never expose cards before close.
  var tableOutcome=shared(current);
  // Reserve only the stake while betting is open. No early winnings or outcome leak.
  long payout=0,balance=w.balanceCents()-bet,revealAt=close;
  var result=new Round(requestId,mode,tableRoundId,revealAt,side,bet,payout,w.balanceCents(),balance,w.revision()+1,time,null);
  game.db().update("UPDATE "+game.table("wallets")+" SET balance=?,revision=?,last_spin=? WHERE player_id=?",balance,result.revision(),time,u.id());
  var agent=u.role()==Accounts.Role.AGENT?u:(u.role()==Accounts.Role.PLAYER&&u.parentId()!=null?accounts.user(u.parentId()):null);
  game.db().update("INSERT INTO "+game.table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type,game_round_id,dt_settled) VALUES(?,?,?,?,?,?,?,FALSE,?,?,?,?,?,?,FALSE)",
   u.id(),requestId,agent==null?null:agent.id(),u.role()==Accounts.Role.SUPER_AGENT?u.id():(agent==null?null:agent.parentId()),bet,bet,payout,result.revision(),game.encode(result),time,tableOutcome.dragon().size()==1?"DT_ONE_CARD_V3":"DT_TWO_CARD_V2","DRAGON_TIGER",tableRoundId);
  return visible(result,time);
 }
 @Transactional public void settle(String mode,String playerId,long time){
  var game=selected(mode);var wallet=game.wallet(playerId,true);
  var rows=game.db().query("SELECT response_json FROM "+game.table("round_ledger")+" WHERE player_id=? AND game_type='DRAGON_TIGER' AND dt_settled=FALSE AND game_round_id<=? ORDER BY wallet_revision",(r,n)->game.decode(r.getString(1),Round.class),playerId,Math.floorDiv(time-BETTING_MS,CYCLE_MS));
  long balance=wallet.balanceCents(),revision=wallet.revision();
  for(var row:rows){
   var outcome=shared(row.tableRoundId());long payout=DragonTigerEngine.payout(row.betCents(),row.side(),outcome.winner());balance=Math.addExact(balance,payout);revision++;
   var settled=new Round(row.requestId(),mode,row.tableRoundId(),row.revealAt(),row.side(),row.betCents(),payout,row.balanceBeforeCents(),balance,revision,row.createdAt(),outcome);
   game.db().update("UPDATE "+game.table("round_ledger")+" SET dt_settled=TRUE,payout_cents=?,response_json=? WHERE player_id=? AND request_id=?",payout,game.encode(settled),playerId,row.requestId());
  }
  if(!rows.isEmpty())game.db().update("UPDATE "+game.table("wallets")+" SET balance=?,revision=? WHERE player_id=?",balance,revision,playerId);
 }
 public List<String> pendingPlayers(String mode){var game=selected(mode);return game.db().queryForList("SELECT DISTINCT player_id FROM "+game.table("round_ledger")+" WHERE dt_settled=FALSE AND game_round_id<=? LIMIT 100",String.class,Math.floorDiv(now()-BETTING_MS,CYCLE_MS));}
 @Transactional public List<Round> bets(Accounts.User actor,String mode,long id){accounts.requireGame(actor);settle(mode,actor.id(),now());var game=selected(mode);return game.db().query("SELECT response_json FROM "+game.table("round_ledger")+" WHERE player_id=? AND game_type='DRAGON_TIGER' AND game_round_id=? ORDER BY wallet_revision",(r,n)->visible(game.decode(r.getString(1),Round.class),now()),actor.id(),id);}
 @Transactional public List<Round> history(Accounts.User actor,String mode){return historyAt(actor,mode,now());}
 @Transactional public List<Round> historyAt(Accounts.User actor,String mode,long time){
  accounts.requireGame(actor);settle(mode,actor.id(),time);var game=selected(mode);String ledger=game.table("round_ledger");
  // Limit distinct games, not individual clicks; every bet in those games must be included.
  return game.db().query("SELECT response_json FROM "+ledger+" WHERE player_id=? AND game_type='DRAGON_TIGER' AND dt_settled=TRUE AND game_round_id IN (SELECT game_round_id FROM (SELECT DISTINCT game_round_id FROM "+ledger+" WHERE player_id=? AND game_type='DRAGON_TIGER' AND dt_settled=TRUE AND game_round_id<=? ORDER BY game_round_id DESC LIMIT 20) recent_games) ORDER BY game_round_id DESC,wallet_revision DESC",(r,n)->game.decode(r.getString(1),Round.class),actor.id(),actor.id(),Math.floorDiv(time-BETTING_MS,CYCLE_MS));
 }
}
