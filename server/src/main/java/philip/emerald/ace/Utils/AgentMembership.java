package philip.emerald.ace.Utils;
import philip.emerald.ace.superace.GameService;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgentMembership {
 private final JdbcTemplate db;
 private final Accounts accounts;
 public AgentMembership(JdbcTemplate db,Accounts accounts){this.db=db;this.accounts=accounts;}
 @Transactional public void remove(Accounts.User actor,String playerId,String requestId){
  accounts.hierarchyLock();actor=accounts.user(actor.id());accounts.require(actor,Accounts.Role.AGENT);
  if(!actor.enabled())throw Accounts.unauthorized();
  // An old retry must not remove the Player again after they rejoin this Agent.
  var prior=db.queryForList("SELECT actor_id,target_id,action_name FROM audit_log WHERE id=?",requestId);
  if(!prior.isEmpty()){
   var record=prior.getFirst();
   if(!actor.id().equals(record.get("actor_id"))||!playerId.equals(record.get("target_id"))||!"REMOVE_PLAYER".equals(record.get("action_name")))throw GameService.error(409,"REQUEST_REUSED");
   return;
  }
  var player=accounts.user(playerId);
  if(player.role()!=Accounts.Role.PLAYER||!actor.id().equals(player.parentId()))throw GameService.error(403,"FORBIDDEN");
  // Club membership is independent of Agent ownership. Keep club_id and wallets.
  db.update("UPDATE accounts SET parent_id=NULL WHERE id=?",playerId);
  db.update("UPDATE agent_join_requests SET status='REMOVED',decided_at=? WHERE player_id=?",System.currentTimeMillis(),playerId);
  db.update("INSERT INTO audit_log(id,actor_id,action_name,target_id,detail_text,created_at) VALUES(?,?,'REMOVE_PLAYER',?,?,?)",requestId,actor.id(),playerId,"Removed Agent ownership; club membership unchanged",System.currentTimeMillis());
 }
 public record Request(String id,String playerId,String playerCode,String username,String agentCode,String status,long createdAt){}
 public List<Request> list(Accounts.User actor){
  if(actor.role()!=Accounts.Role.PLAYER&&actor.role()!=Accounts.Role.AGENT)throw GameService.error(403,"FORBIDDEN");
  return db.query("SELECT j.*,p.public_code AS player_code,p.username,a.public_code AS agent_code FROM agent_join_requests j JOIN accounts p ON p.id=j.player_id JOIN accounts a ON a.id=j.agent_id WHERE "+(actor.role()==Accounts.Role.PLAYER?"j.player_id=?":"j.agent_id=? AND j.status='PENDING'")+" ORDER BY j.created_at",(r,n)->new Request(r.getString("request_id"),r.getString("player_id"),r.getString("player_code"),r.getString("username"),r.getString("agent_code"),r.getString("status"),r.getLong("created_at")),actor.id());
 }
 @Transactional public void request(Accounts.User actor,String code){
  accounts.hierarchyLock();actor=accounts.user(actor.id());
  accounts.require(actor,Accounts.Role.PLAYER);
  if(!actor.enabled())throw Accounts.unauthorized();
  var targets=db.queryForList("SELECT id FROM accounts WHERE public_code=? AND role='AGENT' AND enabled=TRUE",String.class,code);
  if(targets.isEmpty())throw GameService.error(400,"INVALID_PARENT");
  String agent=targets.getFirst();
  if(agent.equals(actor.parentId()))throw GameService.error(409,"ALREADY_MEMBER");
  var pending=db.queryForList("SELECT agent_id FROM agent_join_requests WHERE player_id=? AND status='PENDING'",String.class,actor.id());
  if(!pending.isEmpty()){if(pending.getFirst().equals(agent))return;throw GameService.error(409,"JOIN_PENDING");}
  db.update("DELETE FROM agent_join_requests WHERE player_id=?",actor.id());
  db.update("INSERT INTO agent_join_requests(request_id,player_id,agent_id,previous_parent_id,status,created_at) VALUES(?,?,?,?,'PENDING',?)",UUID.randomUUID().toString(),actor.id(),agent,actor.parentId(),System.currentTimeMillis());
  accounts.audit(actor.id(),"REQUEST_AGENT",agent,"Player requested membership");
 }
 @Transactional public void decide(Accounts.User actor,String requestId,boolean approve){
  accounts.hierarchyLock();actor=accounts.user(actor.id());accounts.require(actor,Accounts.Role.AGENT);
  if(!actor.enabled())throw Accounts.unauthorized();
  var rows=db.queryForList("SELECT * FROM agent_join_requests WHERE request_id=? AND agent_id=? FOR UPDATE",requestId,actor.id());
  if(rows.isEmpty())throw GameService.error(404,"NOT_FOUND");
  var row=rows.getFirst();String result=approve?"APPROVED":"REJECTED";
  String playerId=(String)row.get("player_id");
  if(!"PENDING".equals(row.get("status"))){if(result.equals(row.get("status")))return;throw GameService.error(409,"ALREADY_DECIDED");}
  var player=accounts.user(playerId);
  if(approve){
   if(!player.enabled()||player.role()!=Accounts.Role.PLAYER||!Objects.equals(player.parentId(),row.get("previous_parent_id")))throw GameService.error(409,"MEMBERSHIP_CHANGED");
   db.update("UPDATE accounts SET parent_id=? WHERE id=?",actor.id(),playerId);
  }
  db.update("UPDATE agent_join_requests SET status=?,decided_at=? WHERE player_id=?",result,System.currentTimeMillis(),playerId);
  accounts.audit(actor.id(),result+"_AGENT_JOIN",playerId,"Previous parent="+player.parentId());
 }
}
