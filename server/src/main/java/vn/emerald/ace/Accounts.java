package vn.emerald.ace;

import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class Accounts {
 public enum Role {CREATOR,SUPER_AGENT,AGENT,PLAYER}
 public record User(String id,String username,String displayName,Role role,String parentId,boolean enabled,Integer commissionBps,String publicCode){}
 public record Auth(User user,String csrf){}
 public record Login(Auth auth,String token,String refresh){}
 private final JdbcTemplate db;private final BCryptPasswordEncoder passwords=new BCryptPasswordEncoder(12);
 private final JwtTokens jwt; private final java.security.SecureRandom rng=new java.security.SecureRandom();
 public Accounts(JdbcTemplate db,JwtTokens jwt){this.db=db;this.jwt=jwt;}
 User map(java.sql.ResultSet r,int n)throws java.sql.SQLException{return new User(r.getString("id"),r.getString("username"),r.getString("display_name"),Role.valueOf(r.getString("role")),r.getString("parent_id"),r.getBoolean("enabled"),(Integer)r.getObject("commission_bps"),r.getString("public_code"));}
 public User user(String id){return db.query("SELECT * FROM accounts WHERE id=?",this::map,id).stream().findFirst().orElseThrow(()->new GameService.ApiError(404,"NOT_FOUND","Không tìm thấy tài khoản."));}
 public Auth authenticate(String cookie){if(cookie==null||cookie.length()>4096)throw unauthorized();var claims=jwt.verify(cookie);var list=db.query("SELECT a.*,s.csrf AS session_csrf FROM sessions s JOIN accounts a ON a.id=s.account_id WHERE s.token_hash=? AND a.id=? AND s.expires_at>? AND a.enabled=TRUE",(r,n)->new Auth(map(r,n),r.getString("session_csrf")),GameService.hash(claims.getId()),claims.getSubject(),System.currentTimeMillis());return list.stream().findFirst().orElseThrow(Accounts::unauthorized);}
 static GameService.ApiError unauthorized(){return new GameService.ApiError(401,"LOGIN_REQUIRED","Vui lòng đăng nhập.");}
 public void csrf(Auth auth,String csrf){if(csrf==null||!MessageDigest.isEqual(auth.csrf().getBytes(StandardCharsets.UTF_8),csrf.getBytes(StandardCharsets.UTF_8)))throw new GameService.ApiError(403,"CSRF","Phiên không khớp. Tải lại trang.");}
 @Transactional(noRollbackFor=GameService.ApiError.class) public Login login(String username,String password){
  username=username.toLowerCase(Locale.ROOT);var rows=db.queryForList("SELECT * FROM accounts WHERE username=? FOR UPDATE",username);
  if(rows.isEmpty())throw new GameService.ApiError(401,"BAD_LOGIN","Tên đăng nhập hoặc mật khẩu không đúng.");var row=rows.getFirst();long now=System.currentTimeMillis();
  if(((Number)row.get("locked_until")).longValue()>now)throw new GameService.ApiError(429,"LOGIN_LOCKED","Tài khoản tạm khóa đăng nhập 15 phút sau nhiều lần sai.");
  if(!user((String)row.get("id")).enabled()||!passwords.matches(password,(String)row.get("password_hash"))){int failed=((Number)row.get("failed_logins")).intValue()+1;db.update("UPDATE accounts SET failed_logins=?,locked_until=? WHERE id=?",failed>=5?0:failed,failed>=5?now+900000:0,row.get("id"));throw new GameService.ApiError(401,"BAD_LOGIN","Tên đăng nhập hoặc mật khẩu không đúng.");}
  User user=user((String)row.get("id"));db.update("UPDATE accounts SET failed_logins=0,locked_until=0 WHERE id=?",user.id());return issue(user);
 }
 public void logout(String token){if(token!=null){String key=GameService.hash(jwt.verify(token).getId());db.update("DELETE FROM refresh_tokens WHERE session_hash=?",key);db.update("DELETE FROM sessions WHERE token_hash=?",key);}}
 Login issue(User u){String sid=GameService.token(),csrf=GameService.token(),refresh=GameService.token();long until=System.currentTimeMillis()+7L*86400000;db.update("INSERT INTO sessions(token_hash,account_id,csrf,expires_at) VALUES(?,?,?,?)",GameService.hash(sid),u.id(),csrf,until);db.update("INSERT INTO refresh_tokens VALUES(?,?,?,?)",GameService.hash(refresh),u.id(),GameService.hash(sid),until);return new Login(new Auth(u,csrf),jwt.issue(u,sid),refresh);}
 @Transactional public Login refresh(String token){hierarchyLock();if(token==null)throw unauthorized();var rows=db.queryForList("SELECT * FROM refresh_tokens WHERE token_hash=? AND expires_at>? FOR UPDATE",GameService.hash(token),System.currentTimeMillis());if(rows.isEmpty())throw unauthorized();var r=rows.getFirst();User u=user((String)r.get("account_id"));if(!u.enabled()||db.queryForObject("SELECT COUNT(*) FROM sessions WHERE token_hash=?",Integer.class,r.get("session_hash"))==0)throw unauthorized();db.update("DELETE FROM refresh_tokens WHERE token_hash=?",GameService.hash(token));db.update("DELETE FROM sessions WHERE token_hash=?",r.get("session_hash"));return issue(u);}
 void hierarchyLock(){db.queryForObject("SELECT id FROM hierarchy_lock WHERE id=1 FOR UPDATE",Integer.class);}
 String code(){for(int i=0;i<10000;i++){String c=String.format(Locale.ROOT,"%06d",rng.nextInt(1000000));if(db.queryForObject("SELECT COUNT(*) FROM accounts WHERE public_code=?",Integer.class,c)==0)return c;}throw GameService.error(503,"CODES_EXHAUSTED");}
 void initAccount(String id){if(db.queryForObject("SELECT COUNT(*) FROM lobby_wallets WHERE player_id=?",Integer.class,id)==0)db.update("INSERT INTO lobby_wallets(player_id,balance) SELECT id,CASE WHEN role='PLAYER' THEN 1000000 ELSE 0 END FROM accounts WHERE id=?",id);if(db.queryForObject("SELECT public_code FROM accounts WHERE id=?",String.class,id)==null)db.update("UPDATE accounts SET public_code=? WHERE id=?",code(),id);if(db.queryForObject("SELECT COUNT(*) FROM wallets WHERE player_id=?",Integer.class,id)==0)db.update("INSERT INTO wallets(player_id,balance,free_spins,locked_bet,revision,last_spin) VALUES(?,0,0,0,0,0)",id);}
 @Transactional public Login register(String username,String name,String password,String agentCode){hierarchyLock();var parents=db.query("SELECT * FROM accounts WHERE public_code=? AND role='AGENT' AND enabled=TRUE",this::map,agentCode);if(parents.isEmpty())throw GameService.error(400,"INVALID_PARENT");User p=parents.getFirst();User u=create(p,username,name,password,Role.PLAYER,p.id());return issue(u);}
 @Transactional public User change(User actor,String targetId,Role role,String parentId,boolean enabled){
  hierarchyLock();actor=user(actor.id());User target=user(targetId);
  if(db.queryForObject("SELECT COUNT(*) FROM direct_registration WHERE agent_id=? OR super_agent_id=?",Integer.class,targetId,targetId)>0)throw GameService.error(409,"SYSTEM_GROUP");
  if(!actor.enabled()||actor.id().equals(targetId)||actor.role()==Role.PLAYER||actor.role()==Role.AGENT||!inScope(actor,target))throw GameService.error(403,"FORBIDDEN");
  if(actor.role()!=Role.CREATOR&&(target.role().ordinal()<=actor.role().ordinal()||role.ordinal()<=actor.role().ordinal()))throw GameService.error(403,"FORBIDDEN");
  if(role==Role.CREATOR){parentId=null;}else {User parent=user(parentId);if(parent.id().equals(targetId)||!parent.enabled()||parent.role().ordinal()!=role.ordinal()-1||!inScope(actor,parent)||inScope(target,parent))throw GameService.error(403,"INVALID_PARENT");}
  if(role!=target.role()&&db.queryForObject("SELECT COUNT(*) FROM accounts WHERE parent_id=?",Integer.class,targetId)>0)throw GameService.error(409,"MOVE_CHILDREN_FIRST");
  db.queryForObject("SELECT balance FROM wallets WHERE player_id=? FOR UPDATE",Long.class,targetId);
  db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id=? FOR UPDATE",Long.class,targetId);
  if(role!=target.role()&&(db.queryForObject("SELECT free_spins FROM lobby_wallets WHERE player_id=?",Integer.class,targetId)>0||db.queryForObject("SELECT COUNT(*) FROM lobby_auto_jobs WHERE player_id=? AND active=TRUE",Integer.class,targetId)>0))throw GameService.error(409,"FINISH_GAME_FIRST");
  if(!enabled)db.update("UPDATE lobby_auto_jobs SET active=FALSE,reason='ACCOUNT_DISABLED' WHERE player_id=?",targetId);
  if(role!=target.role()&&(db.queryForObject("SELECT free_spins FROM wallets WHERE player_id=?",Integer.class,targetId)>0||db.queryForObject("SELECT COUNT(*) FROM auto_jobs WHERE player_id=? AND active=TRUE",Integer.class,targetId)>0))throw GameService.error(409,"FINISH_GAME_FIRST");
  if(!enabled)db.update("UPDATE auto_jobs SET active=FALSE,reason='ACCOUNT_DISABLED' WHERE player_id=?",targetId);
  db.update("UPDATE accounts SET role=?,parent_id=?,enabled=? WHERE id=?",role.name(),parentId,enabled,targetId);db.update("DELETE FROM sessions WHERE account_id=?",targetId);db.update("DELETE FROM refresh_tokens WHERE account_id=?",targetId);audit(actor.id(),"CHANGE_ACCOUNT",targetId,target.role()+" -> "+role+"; parent="+parentId+"; enabled="+enabled);return user(targetId);
 }
 public void require(User u,Role role){if(u.role()!=role)throw new GameService.ApiError(403,"FORBIDDEN","Không có quyền thực hiện thao tác.");}
 public boolean inScope(User actor,User target){if(actor.role()==Role.CREATOR||actor.id().equals(target.id()))return true;User cursor=target;for(int i=0;i<4&&cursor.parentId()!=null;i++){if(cursor.parentId().equals(actor.id()))return true;cursor=user(cursor.parentId());}return false;}
 public List<User> list(User actor){return db.query("SELECT * FROM accounts ORDER BY created_at,id",this::map).stream().filter(u->inScope(actor,u)).toList();}
 @Transactional public User create(User actor,String username,String displayName,String password,Role role,String parentId){
  hierarchyLock();actor=user(actor.id());if(!actor.enabled())throw unauthorized();
  validatePassword(password);if(!username.matches("[a-zA-Z0-9_]{3,40}"))throw new GameService.ApiError(400,"INVALID_NAME","Tên đăng nhập gồm 3–40 chữ, số hoặc dấu gạch dưới.");
  if(role==Role.CREATOR||actor.role().ordinal()>=role.ordinal())throw new GameService.ApiError(403,"FORBIDDEN","Chỉ tạo được cấp thấp hơn trong nhánh của mình.");
  User parent=user(parentId);if(parent.role().ordinal()!=role.ordinal()-1||!parent.enabled()||!inScope(actor,parent))throw new GameService.ApiError(403,"INVALID_PARENT","Cấp quản lý hoặc nhánh không hợp lệ.");
  String id=UUID.randomUUID().toString();db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,parent_id,enabled,created_at) VALUES(?,?,?,?,?,?,TRUE,?)",id,username.toLowerCase(Locale.ROOT),displayName,passwords.encode(password),role.name(),parentId,System.currentTimeMillis());
  if(role==Role.PLAYER)db.update("INSERT INTO wallets(player_id,balance,free_spins,locked_bet,revision,last_spin) VALUES(?,0,0,0,0,0)",id);
  initAccount(id);audit(actor.id(),"CREATE_ACCOUNT",id,role.name());return user(id);
 }
 @Transactional public void password(Auth auth,String oldPassword,String newPassword){hierarchyLock();validatePassword(newPassword);String hash=db.queryForObject("SELECT password_hash FROM accounts WHERE id=?",String.class,auth.user().id());if(!passwords.matches(oldPassword,hash))throw new GameService.ApiError(400,"BAD_PASSWORD","Mật khẩu hiện tại không đúng.");db.update("UPDATE accounts SET password_hash=? WHERE id=?",passwords.encode(newPassword),auth.user().id());db.update("DELETE FROM sessions WHERE account_id=?",auth.user().id());db.update("DELETE FROM refresh_tokens WHERE account_id=?",auth.user().id());audit(auth.user().id(),"CHANGE_PASSWORD",auth.user().id(),"Sessions revoked");}
 static void validatePassword(String p){if(p==null||p.length()<6||p.getBytes(StandardCharsets.UTF_8).length>72)throw new GameService.ApiError(400,"PASSWORD_LENGTH","Mật khẩu ít nhất 6 ký tự và tối đa 72 byte UTF-8.");}
 @Transactional public void bootstrap(String username,String password){
  hierarchyLock();
  for(String existing:db.queryForList("SELECT id FROM accounts WHERE public_code IS NULL",String.class))initAccount(existing);
  validatePassword(password);
  long creators=db.queryForObject("SELECT COUNT(*) FROM accounts WHERE role='CREATOR'",Long.class);
  if(creators==0){
   String id=UUID.randomUUID().toString();
   db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,enabled,created_at) VALUES(?,?,?,?,'CREATOR',TRUE,?)",id,username.toLowerCase(Locale.ROOT),"Creator",passwords.encode(password),System.currentTimeMillis());
   initAccount(id);
  }
  // Keep the documented default Creator available on upgrades without replacing an existing owner.
  if(db.queryForObject("SELECT COUNT(*) FROM accounts WHERE username='zuchiha'",Long.class)==0){
   String id=UUID.randomUUID().toString();
   db.update("INSERT INTO accounts(id,username,display_name,password_hash,role,enabled,created_at) VALUES(?,?,?,?,'CREATOR',TRUE,?)",id,"zuchiha","Creator",passwords.encode("112357"),System.currentTimeMillis());
   initAccount(id);
  }
 }
 public void audit(String actor,String action,String target,String details){db.update("INSERT INTO audit_log(id,actor_id,action_name,target_id,detail_text,created_at) VALUES(?,?,?,?,?,?)",UUID.randomUUID().toString(),actor,action,target,details,System.currentTimeMillis());}
}
