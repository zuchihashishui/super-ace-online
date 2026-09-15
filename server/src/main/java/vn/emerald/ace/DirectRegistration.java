package vn.emerald.ace;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.Objects;

/** Direct signups retain a valid accounting hierarchy without asking for a referral. */
@Service
public class DirectRegistration {
 private final Accounts accounts;
 private final JdbcTemplate db;
 public DirectRegistration(Accounts accounts,JdbcTemplate db){this.accounts=accounts;this.db=db;}
 @Transactional public Accounts.Login register(String username,String password){
  accounts.hierarchyLock();
  Accounts.validatePassword(password);
  var agent=initialize();
  var player=accounts.create(agent,username,username,password,Accounts.Role.PLAYER,agent.id());
  return accounts.issue(player);
 }
 @Transactional public Accounts.User initialize(){
  accounts.hierarchyLock();
  var creators=db.query("SELECT * FROM accounts WHERE role='CREATOR' AND enabled=TRUE ORDER BY CASE WHEN username='zuchiha' THEN 0 ELSE 1 END, created_at,id",accounts::map);
  if(creators.isEmpty())throw GameService.error(503,"REGISTRATION_UNAVAILABLE");
  var creator=creators.getFirst();
  var superAgent=ensure(creator,"zuchiha1","Super Agent",Accounts.Role.SUPER_AGENT,creator.id());
  var agent=ensure(creator,"zuchiha2","Agent",Accounts.Role.AGENT,superAgent.id());
  ensure(creator,"zuchiha3","Player",Accounts.Role.PLAYER,agent.id());
  db.update("UPDATE accounts SET commission_bps=0 WHERE id=?",agent.id());
  if(db.queryForObject("SELECT COUNT(*) FROM direct_registration WHERE id=1",Integer.class)==0)db.update("INSERT INTO direct_registration(id,agent_id,super_agent_id) VALUES(1,?,?)",agent.id(),superAgent.id());
  else db.update("UPDATE direct_registration SET agent_id=?,super_agent_id=? WHERE id=1",agent.id(),superAgent.id());
  db.update("UPDATE clubs SET owner_id=?,created_at=CASE WHEN created_at=0 THEN ? ELSE created_at END WHERE public_code='686868'",creator.id(),System.currentTimeMillis());
  return agent;
 }
 private Accounts.User ensure(Accounts.User creator,String username,String display,Accounts.Role role,String parentId){
  var rows=db.query("SELECT * FROM accounts WHERE username=?",accounts::map,username);
  if(rows.isEmpty())return accounts.create(creator,username,display,"112357",role,parentId);
  var user=rows.getFirst();if(user.role()!=role||!Objects.equals(user.parentId(),parentId))db.update("UPDATE accounts SET role=?,parent_id=?,enabled=TRUE WHERE id=?",role.name(),parentId,user.id());
  accounts.initAccount(user.id());return accounts.user(user.id());
 }
 private String systemName(){return "sys_"+UUID.randomUUID().toString().replace("-","");}
}
