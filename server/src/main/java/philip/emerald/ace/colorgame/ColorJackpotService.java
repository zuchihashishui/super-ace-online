package philip.emerald.ace.colorgame;

import philip.emerald.ace.Utils.Accounts;
import philip.emerald.ace.superace.GameService;
import philip.emerald.ace.superace.LobbyGameService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.security.SecureRandom;
import java.util.*;

@Service
public class ColorJackpotService {
 private final GameService club;private final LobbyGameService lobby;private final Accounts accounts;private final TransactionTemplate tx;
 private final SecureRandom random=new SecureRandom();
 public ColorJackpotService(GameService club,LobbyGameService lobby,Accounts accounts,PlatformTransactionManager manager){this.club=club;this.lobby=lobby;this.accounts=accounts;this.tx=new TransactionTemplate(manager);}
 private GameService game(String mode){return switch(mode){case "LOBBY"->lobby;case "CLUB"->club;default->throw GameService.error(400,"INVALID_MODE");};}
 public record Level(String tier,int prizeBps,int probabilityBps){}
 public record Pool(String mode,boolean enabled,long balanceCents,long contributedCents,long awardedCents,long revision,int rulesVersion,int contributionBps,String targetColor,List<Level> levels){}
 public record Award(String id,String mode,long roundId,long amountCents,String tier,long createdAt,boolean read){}
 public record Win(long roundId,String tier,long awardCents,long poolBeforeCents,long poolAfterCents,long settledAt){}
 private Pool pool(String mode,boolean lock){game(mode);return club.db().queryForObject("SELECT * FROM color_jackpot_pools WHERE mode=?"+(lock?" FOR UPDATE":""),(r,n)->new Pool(mode,r.getBoolean("enabled"),r.getLong("balance_cents"),r.getLong("contributed_cents"),r.getLong("awarded_cents"),r.getLong("revision"),1,100,"BLUE",Arrays.stream(ColorJackpotMath.Tier.values()).map(t->new Level(t.name(),t.prizeBps,t.weight)).toList()),mode);}
 /** Called BEFORE the wallet lock; settlement uses pool -> round -> wallets. */
 @Transactional(propagation=Propagation.MANDATORY) public void lockBetRound(String mode,long id,long time){
  game(mode);if(id!=Math.floorDiv(time,ColorGameService.CYCLE_MS)||time>=id*ColorGameService.CYCLE_MS+ColorGameService.BETTING_MS)return;
  boolean enabled=pool(mode,false).enabled();
  try{club.db().update("INSERT INTO color_jackpot_rounds(mode,round_id,enabled) VALUES(?,?,?)",mode,id,enabled);}catch(DuplicateKeyException ignored){}
  club.db().queryForObject("SELECT round_id FROM color_jackpot_rounds WHERE mode=? AND round_id=? FOR UPDATE",Long.class,mode,id);
 }
 @Transactional(propagation=Propagation.MANDATORY) public void recordBet(String mode,long round,String player,String request,long cents){
  club.db().update("INSERT INTO color_jackpot_bets(mode,player_id,request_id,round_id,stake_cents) VALUES(?,?,?,?,?)",mode,player,request,round,cents);
 }
 public Pool snapshot(String mode){game(mode);settleDue(mode,System.currentTimeMillis());return pool(mode,false);}
 public void settleDue(String mode,long time){game(mode);for(int i=0;i<30;i++){Boolean done=tx.execute(status->settleOne(mode,time));if(!Boolean.TRUE.equals(done))break;}}
 private boolean settleOne(String mode,long time){
  Pool p=pool(mode,true);
  var pending=club.db().queryForList("SELECT round_id,enabled FROM color_jackpot_rounds WHERE mode=? AND settled=FALSE AND round_id<=? ORDER BY round_id LIMIT 1 FOR UPDATE",mode,Math.floorDiv(time-ColorGameService.BETTING_MS,ColorGameService.CYCLE_MS));
  if(pending.isEmpty())return false;long id=((Number)pending.getFirst().get("round_id")).longValue();boolean enabled=(Boolean)pending.getFirst().get("enabled");
  var stakes=new TreeMap<String,Long>();club.db().query("SELECT player_id,SUM(stake_cents) total FROM color_jackpot_bets WHERE mode=? AND round_id=? GROUP BY player_id ORDER BY player_id",(org.springframework.jdbc.core.RowCallbackHandler)r->stakes.put(r.getString(1),r.getLong(2)),mode,id);
  long total=0;for(long n:stakes.values())total=Math.addExact(total,n);
  int remainder=club.db().queryForObject("SELECT remainder_units FROM color_jackpot_pools WHERE mode=?",Integer.class,mode);
  var contribution=enabled?ColorJackpotMath.contribution(total,remainder):new ColorJackpotMath.Contribution(0,remainder);
  long before=Math.addExact(p.balanceCents(),contribution.cents()),prize=0;ColorJackpotMath.Tier tier=null;
  if(enabled&&!stakes.isEmpty()){
   var outcome=club.decode(club.db().queryForObject("SELECT outcome_json FROM color_game_rounds WHERE round_id=?",String.class,id),ColorGameEngine.Outcome.class);
   if(outcome.dice().stream().allMatch(c->c==ColorGameEngine.Side.BLUE)){tier=ColorJackpotMath.tier(random.nextInt(10000));prize=ColorJackpotMath.prize(before,tier);}
  }
  long after=before-prize;
  if(contribution.cents()>0)journal(mode,"ROUND-"+id+"-CONTRIBUTION",null,id,"CONTRIBUTION",contribution.cents(),before,time);
  if(prize>0){
   var selected=game(mode);for(var share:ColorJackpotMath.split(prize,stakes).entrySet()){if(share.getValue()==0)continue;
    var wallet=selected.wallet(share.getKey(),true);long balance=Math.addExact(wallet.balanceCents(),share.getValue());
    selected.db().update("UPDATE "+selected.table("wallets")+" SET balance=?,revision=revision+1 WHERE player_id=?",balance,share.getKey());
    club.db().update("INSERT INTO color_jackpot_awards(id,mode,round_id,player_id,stake_cents,amount_cents,tier,created_at) VALUES(?,?,?,?,?,?,?,?)",UUID.randomUUID().toString(),mode,id,share.getKey(),stakes.get(share.getKey()),share.getValue(),tier.name(),time);
   }
   journal(mode,"ROUND-"+id+"-AWARD",null,id,"AWARD",-prize,after,time);
  }
  club.db().update("UPDATE color_jackpot_pools SET balance_cents=?,remainder_units=?,contributed_cents=?,awarded_cents=?,revision=revision+1 WHERE mode=?",after,contribution.remainder(),Math.addExact(p.contributedCents(),contribution.cents()),Math.addExact(p.awardedCents(),prize),mode);
  club.db().update("UPDATE color_jackpot_rounds SET settled=TRUE,stake_cents=?,contribution_cents=?,pool_before_cents=?,pool_after_cents=?,tier=?,award_cents=?,settled_at=? WHERE mode=? AND round_id=?",total,contribution.cents(),before,after,tier==null?null:tier.name(),prize,time,mode,id);
  return true;
 }
 private void journal(String mode,String request,String actor,Long round,String kind,long cents,long balance,long time){club.db().update("INSERT INTO color_jackpot_journal(id,mode,request_id,actor_id,round_id,kind,amount_cents,balance_after_cents,created_at) VALUES(?,?,?,?,?,?,?,?,?)",UUID.randomUUID().toString(),mode,request,actor,round,kind,cents,balance,time);}
 public List<Award> awards(Accounts.User actor,String mode){accounts.requireGame(actor);game(mode);settleDue(mode,System.currentTimeMillis());return club.db().query("SELECT * FROM color_jackpot_awards WHERE mode=? AND player_id=? ORDER BY created_at DESC,id DESC LIMIT 50",(r,n)->new Award(r.getString("id"),mode,r.getLong("round_id"),r.getLong("amount_cents"),r.getString("tier"),r.getLong("created_at"),r.getObject("read_at")!=null),mode,actor.id());}
 public List<Win> history(String mode){game(mode);return club.db().query("SELECT * FROM color_jackpot_rounds WHERE mode=? AND settled=TRUE AND award_cents>0 ORDER BY round_id DESC LIMIT 20",(r,n)->new Win(r.getLong("round_id"),r.getString("tier"),r.getLong("award_cents"),r.getLong("pool_before_cents"),r.getLong("pool_after_cents"),r.getLong("settled_at")),mode);}
 @Transactional public Pool topUp(Accounts.User actor,String mode,String request,long amount){
  actor=accounts.user(actor.id());accounts.requireGame(actor);accounts.require(actor,Accounts.Role.CREATOR);Pool p=pool(mode,true);if(!p.enabled())throw GameService.error(409,"JACKPOT_DISABLED");
  if(amount<100||amount>100_000_000_00L)throw GameService.error(400,"INVALID_AMOUNT");
  var rows=club.db().queryForList("SELECT actor_id,amount_cents,kind FROM color_jackpot_journal WHERE mode=? AND request_id=?",mode,request);
  if(!rows.isEmpty()){var prior=rows.getFirst();if(!actor.id().equals(prior.get("actor_id"))||!"TOP_UP".equals(prior.get("kind"))||amount!=((Number)prior.get("amount_cents")).longValue())throw GameService.error(409,"REQUEST_REUSED");return p;}
  long balance=Math.addExact(p.balanceCents(),amount);club.db().update("UPDATE color_jackpot_pools SET balance_cents=?,revision=revision+1 WHERE mode=?",balance,mode);journal(mode,request,actor.id(),null,"TOP_UP",amount,balance,System.currentTimeMillis());return pool(mode,false);
 }
}
