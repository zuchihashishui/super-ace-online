package vn.emerald.ace;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class RtpSchedule {
 final JdbcTemplate db;public RtpSchedule(JdbcTemplate db){this.db=db;}
 @Transactional public void initialize(){db.queryForObject("SELECT id FROM hierarchy_lock WHERE id=1 FOR UPDATE",Integer.class);if(db.queryForObject("SELECT COUNT(*) FROM rtp_schedule",Integer.class)==0)db.update("INSERT INTO rtp_schedule VALUES(1,?)",System.currentTimeMillis());}
 public static String profileAt(long start,long now){return now<start+7L*86400000?"INTRO_97":"STANDARD_96";}
 public String active(){return profileAt(db.queryForObject("SELECT starts_at FROM rtp_schedule WHERE id=1",Long.class),System.currentTimeMillis());}
 public Map<String,Object> info(){long start=db.queryForObject("SELECT starts_at FROM rtp_schedule WHERE id=1",Long.class);String profile=active();var totals=db.queryForMap("SELECT COALESCE(SUM(wager_cents),0) wager,COALESCE(SUM(payout_cents),0) payout FROM round_ledger WHERE rtp_profile=?",profile);double wager=((Number)totals.get("wager")).doubleValue(),payout=((Number)totals.get("payout")).doubleValue();Map<String,Object> result=new LinkedHashMap<>();result.put("profile",profile);result.put("payoutScale",GameEngine.scaleFor(profile));result.put("targetPercent",profile.equals("INTRO_97")?97:96);result.put("startsAt",start);result.put("switchAt",start+7L*86400000);result.put("observedPercent",wager==0?null:100*payout/wager);result.put("wagerCents",totals.get("wager"));result.put("payoutCents",totals.get("payout"));return result;}
}
