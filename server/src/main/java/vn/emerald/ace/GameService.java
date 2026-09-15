package vn.emerald.ace;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

@Service @org.springframework.context.annotation.Primary
public class GameService {
 final JdbcTemplate db; final ObjectMapper json; final Accounts accounts; final GameEngine engine=new GameEngine();
 @org.springframework.beans.factory.annotation.Autowired RtpSchedule rtp;
 final Map<String,GameEngine> profiles=Map.of("LOBBY_98",new GameEngine("LOBBY_98"),"CLUB_97",new GameEngine("CLUB_97"),"LEGACY",engine,"INTRO_97",new GameEngine("INTRO_97"),"STANDARD_96",new GameEngine("STANDARD_96"));
 private final long cooldown; private static final SecureRandom RNG=new SecureRandom();
 public GameService(JdbcTemplate db,ObjectMapper json,Accounts accounts,@Value("${ace.spin-cooldown-ms}") long cooldown){this.db=db;this.json=json;this.accounts=accounts;this.cooldown=cooldown;}
 public JdbcTemplate db(){return db;}
 String table(String name){return name;}
 String mode(){return "CLUB";}
 String activeProfile(){return "CLUB_97";}
 public record Wallet(String id,long balanceCents,int freeSpins,long lockedBetCents,long revision,long lastSpin){}
 public record Me(String id,String username,String displayName,Accounts.Role role,String csrf,long balanceCents,int freeSpins,long lockedBetCents,long revision,String publicCode,String rtpProfile,double payoutScale,String mode,long lobbyGoldCents,long clubChipsCents,String clubCode,String clubName){}
 public record SpinResult(String requestId,long betCents,boolean freeSpin,long balanceBeforeCents,long balanceCents,int freeSpins,long lockedBetCents,long revision,GameEngine.Outcome outcome){}
 public static class ApiError extends RuntimeException {public final int status;public final String code;public ApiError(int status,String code,String message){super(message);this.status=status;this.code=code;}}
 static ApiError error(int status,String code){return new ApiError(status,code,code);}
 static String token(){byte[] bytes=new byte[32];RNG.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 static String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 public Wallet wallet(String id,boolean lock){return db.query("SELECT * FROM "+table("wallets")+" WHERE player_id=?"+(lock?" FOR UPDATE":""),(r,n)->new Wallet(r.getString("player_id"),r.getLong("balance"),r.getInt("free_spins"),r.getLong("locked_bet"),r.getLong("revision"),r.getLong("last_spin")),id).stream().findFirst().orElseThrow(()->error(403,"PLAYER_ONLY"));}
 public Me me(Accounts.Auth auth){var u=auth.user();var w=wallet(u.id(),false);String profile=w.freeSpins()>0?db.queryForObject("SELECT bonus_profile FROM "+table("wallets")+" WHERE player_id=?",String.class,u.id()):activeProfile();if(profile==null)profile="LEGACY";return new Me(u.id(),u.username(),u.displayName(),u.role(),auth.csrf(),w.balanceCents(),w.freeSpins(),w.lockedBetCents(),w.revision(),u.publicCode(),profile,GameEngine.scaleFor(profile),mode(),db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id=?",Long.class,u.id()),db.queryForObject("SELECT balance FROM wallets WHERE player_id=?",Long.class,u.id()),db.queryForObject("SELECT c.public_code FROM clubs c JOIN accounts a ON a.club_id=c.id WHERE a.id=?",String.class,u.id()),db.queryForObject("SELECT c.name FROM clubs c JOIN accounts a ON a.club_id=c.id WHERE a.id=?",String.class,u.id()));}
 @Transactional public SpinResult spin(Accounts.User u,String requestId,long bet,long expectedRevision){accounts.require(u,Accounts.Role.PLAYER);Wallet w=wallet(u.id(),true);var prior=saved(u.id(),requestId);if(prior!=null){if(prior.betCents()!=bet)throw error(409,"REQUEST_REUSED");return prior;}if(db.queryForObject("SELECT COUNT(*) FROM "+table("auto_jobs")+" WHERE player_id=? AND active=TRUE",Integer.class,u.id())>0)throw error(409,"AUTO_ACTIVE");if(w.revision()!=expectedRevision)throw error(409,"STALE_STATE");if(System.currentTimeMillis()-w.lastSpin()<cooldown)throw error(429,"TOO_FAST");return executeLocked(u,w,requestId,bet,null);}
 // Called only with the wallet row locked inside the caller's transaction.
 SpinResult executeLocked(Accounts.User u,Wallet w,String requestId,long bet,String runId){
  u=accounts.user(u.id());accounts.require(u,Accounts.Role.PLAYER);if(!u.enabled())throw Accounts.unauthorized();if(!GameEngine.BETS.contains(bet))throw error(400,"INVALID_BET");boolean free=w.freeSpins()>0;
  if(free&&bet!=w.lockedBetCents())throw error(409,"BET_LOCKED");if(!free&&w.balanceCents()<bet)throw error(409,"INSUFFICIENT_FUNDS");
  String profile=free?db.queryForObject("SELECT bonus_profile FROM "+table("wallets")+" WHERE player_id=?",String.class,u.id()):activeProfile();if(profile==null)profile="LEGACY";var outcome=profiles.get(profile).spin(bet);long balance=Math.addExact(w.balanceCents()-(free?0:bet),outcome.winCents());int remaining=w.freeSpins()-(free?1:0)+outcome.freeAward();long locked=remaining>0?bet:0,now=System.currentTimeMillis();
  db.update("UPDATE "+table("wallets")+" SET bonus_profile=? WHERE player_id=?",remaining>0?profile:null,u.id());
  var result=new SpinResult(requestId,bet,free,w.balanceCents(),balance,remaining,locked,w.revision()+1,outcome);
  db.update("UPDATE "+table("wallets")+" SET balance=?,free_spins=?,locked_bet=?,revision=?,last_spin=? WHERE player_id=?",balance,remaining,locked,result.revision(),now,u.id());
  var agent=accounts.user(u.parentId());db.update("INSERT INTO "+table("round_ledger")+"(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,run_id,response_json,created_at,rtp_profile) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",u.id(),requestId,agent.id(),agent.parentId(),bet,free?0:bet,outcome.winCents(),free,result.revision(),runId,encode(result),now,profile);return result;
 }
 SpinResult saved(String id,String requestId){return db.query("SELECT response_json FROM "+table("round_ledger")+" WHERE player_id=? AND request_id=?",(r,n)->decode(r.getString(1),SpinResult.class),id,requestId).stream().findFirst().orElse(null);}
 String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
 <T>T decode(String value,Class<T> type){try{return json.readValue(value,type);}catch(Exception e){throw new IllegalStateException(e);}}
}
