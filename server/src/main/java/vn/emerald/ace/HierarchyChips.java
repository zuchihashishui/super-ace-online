package vn.emerald.ace;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class HierarchyChips {
 final Accounts accounts;final GameService game;
 public HierarchyChips(Accounts accounts,GameService game){this.accounts=accounts;this.game=game;}
 @Transactional public Map<String,Object> move(Accounts.User actor,String id,String targetId,String direction,long amount){
  accounts.hierarchyLock();actor=accounts.user(actor.id());var target=accounts.user(targetId);
  if(!actor.enabled()||actor.role()==Accounts.Role.PLAYER||actor.id().equals(targetId)||!accounts.inScope(actor,target)||!target.enabled())throw GameService.error(403,"FORBIDDEN");
  if(!List.of("GIVE","TAKE","ISSUE").contains(direction)||amount<=0||amount>100000000)throw GameService.error(400,"INVALID_AMOUNT");
  if(direction.equals("ISSUE"))accounts.require(actor,Accounts.Role.CREATOR);
  boolean mint=direction.equals("ISSUE")||(direction.equals("GIVE")&&actor.role()==Accounts.Role.CREATOR);
  var prior=game.db().queryForList("SELECT * FROM chip_movements WHERE id=?",id);if(!prior.isEmpty()){var r=prior.getFirst();if(!actor.id().equals(r.get("actor_id"))||!targetId.equals(r.get("target_id"))||!direction.equals(r.get("direction"))||amount!=((Number)r.get("amount_cents")).longValue())throw GameService.error(409,"REQUEST_REUSED");return Map.of("id",id,"ok",true);}
  List<String> ids=new ArrayList<>(List.of(actor.id(),targetId));Collections.sort(ids);for(String account:ids)game.wallet(account,true);
  String source=direction.equals("TAKE")?targetId:actor.id(),destination=direction.equals("TAKE")?actor.id():targetId;
  if(!mint){if(game.wallet(source,false).balanceCents()<amount)throw GameService.error(409,"INSUFFICIENT_FUNDS");game.db().update("UPDATE wallets SET balance=balance-?,revision=revision+1 WHERE player_id=?",amount,source);}
  game.db().update("UPDATE wallets SET balance=balance+?,revision=revision+1 WHERE player_id=?",amount,destination);
  long now=System.currentTimeMillis();game.db().update("INSERT INTO chip_movements VALUES(?,?,?,?,?,?)",id,actor.id(),targetId,direction,amount,now);
  history(destination,id,"DEPOSIT",amount,actor.id(),now,"Hierarchy "+direction);
  if(!mint)history(source,id,"WITHDRAWAL",amount,actor.id(),now,"Hierarchy "+direction);
  accounts.audit(actor.id(),"CHIP_"+direction,targetId,"amount="+amount+"; request="+id);return Map.of("id",id,"ok",true);
 }
 void history(String account,String request,String kind,long amount,String actor,long now,String note){if(kind.equals("DEPOSIT"))game.db().update("INSERT INTO chip_notifications(id,recipient_id,sender_id,amount_cents,movement_id,created_at) VALUES(?,?,?,?,?,?)",UUID.randomUUID().toString(),account,actor,amount,request,now);game.db().update("INSERT INTO chip_transfers(id,player_id,request_id,kind,amount_cents,status,reference_text,created_at,decided_at,decided_by) VALUES(?,?,?,?,?,'APPROVED',?,?,?,?)",UUID.randomUUID().toString(),account,request,kind,amount,note,now,now,actor);}
}
