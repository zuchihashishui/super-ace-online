package philip.emerald.ace.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import java.security.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
/** Shared wallet storage and receipt primitives. No game outcome selection here. */
public abstract class WalletStore {
 protected final JdbcTemplate db; protected final ObjectMapper json; protected final Accounts accounts;
 private static final SecureRandom RNG=new SecureRandom();
 protected WalletStore(JdbcTemplate db,ObjectMapper json,Accounts accounts){this.db=db;this.json=json;this.accounts=accounts;}
 public JdbcTemplate db(){return db;}
 public String table(String name){return name;}
 public String mode(){return "CLUB";}
 public record Wallet(String id,long balanceCents,int freeSpins,long lockedBetCents,long revision,long lastSpin){}
 public record Me(String id,String username,String displayName,Accounts.Role role,String csrf,long balanceCents,int freeSpins,long lockedBetCents,long revision,String publicCode,String rtpProfile,double payoutScale,String mode,long lobbyGoldCents,long clubChipsCents,String clubCode,String clubName){}
 public static class ApiError extends RuntimeException {public final int status;public final String code;public ApiError(int status,String code,String message){super(message);this.status=status;this.code=code;}}
 public static ApiError error(int status,String code){return new ApiError(status,code,code);}
 public static String token(){byte[] bytes=new byte[32];RNG.nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);}
 public static String hash(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
 public Wallet wallet(String id,boolean lock){return db.query("SELECT * FROM "+table("wallets")+" WHERE player_id=?"+(lock?" FOR UPDATE":""),(r,n)->new Wallet(r.getString("player_id"),r.getLong("balance"),r.getInt("free_spins"),r.getLong("locked_bet"),r.getLong("revision"),r.getLong("last_spin")),id).stream().findFirst().orElseThrow(()->error(403,"PLAYER_ONLY"));}
 public String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
 public <T>T decode(String value,Class<T> type){try{return json.readValue(value,type);}catch(Exception e){throw new IllegalStateException(e);}}
}
