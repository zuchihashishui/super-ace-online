package philip.emerald.ace.superace;
import philip.emerald.ace.Utils.Accounts;

import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RtpSettings {
 private final JdbcTemplate db;
 private final Accounts accounts;
 public RtpSettings(JdbcTemplate db,Accounts accounts){this.db=db;this.accounts=accounts;}
 public record Setting(String mode,int targetBps,long revision){
  public BigDecimal targetPercent(){return BigDecimal.valueOf(targetBps,2);}
  public String profile(){return "RTP_"+targetBps;}
 }
 public Setting get(String mode){
  if(!"LOBBY".equals(mode)&&!"CLUB".equals(mode))throw GameService.error(400,"INVALID_MODE");
  return db.queryForObject("SELECT * FROM rtp_settings WHERE mode=?",(r,n)->new Setting(r.getString("mode"),r.getInt("target_bps"),r.getLong("revision")),mode);
 }
 @Transactional public Setting update(Accounts.User actor,String mode,BigDecimal percent,long revision){
  accounts.hierarchyLock();actor=accounts.user(actor.id());accounts.require(actor,Accounts.Role.CREATOR);
  if(!actor.enabled())throw Accounts.unauthorized();
  int bps;
  try{bps=percent.movePointRight(2).intValueExact();}catch(ArithmeticException|NullPointerException e){throw GameService.error(400,"INVALID_RTP");}
  if(bps<100||bps>10000)throw GameService.error(400,"INVALID_RTP");
  var old=get(mode);
  if(db.update("UPDATE rtp_settings SET target_bps=?,revision=revision+1 WHERE mode=? AND revision=?",bps,mode,revision)!=1)throw GameService.error(409,"STALE_STATE");
  accounts.audit(actor.id(),"UPDATE_RTP",mode,old.targetPercent()+" -> "+BigDecimal.valueOf(bps,2));
  return get(mode);
 }
}
