package philip.emerald.ace.Utils;
import philip.emerald.ace.superace.GameService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class ChipNotifications {
 private final JdbcTemplate db;
 public ChipNotifications(JdbcTemplate db){this.db=db;}
 public record Notice(String id,long amountCents,String senderName,long createdAt,String currency,String kind,String mode,String tier,Long roundId){
  public Notice(String id,long amount,String sender,long created,String currency){this(id,amount,sender,created,currency,"TRANSFER",currency.equals("GOLD")?"LOBBY":"CLUB",null,null);}
 }
 public List<Notice> unread(Accounts.User user){var notices=new ArrayList<>(db.query("SELECT n.*,a.display_name FROM chip_notifications n JOIN accounts a ON a.id=n.sender_id WHERE n.recipient_id=? AND n.read_at IS NULL ORDER BY n.created_at,n.id LIMIT 50",(r,n)->new Notice(r.getString("id"),r.getLong("amount_cents"),r.getString("display_name"),r.getLong("created_at"),"CHIPS"),user.id()));
 notices.addAll(db.query("SELECT * FROM lobby_daily_rewards WHERE player_id=? AND read_at IS NULL ORDER BY reward_date LIMIT 50",(r,n)->new Notice(r.getString("id"),r.getLong("amount_cents"),r.getString("reward_date"),r.getLong("created_at"),"GOLD"),user.id()));
 notices.addAll(db.query("SELECT * FROM color_jackpot_awards WHERE player_id=? AND read_at IS NULL ORDER BY created_at,id LIMIT 50",(r,n)->new Notice(r.getString("id"),r.getLong("amount_cents"),"Color Game",r.getLong("created_at"),r.getString("mode").equals("LOBBY")?"GOLD":"CHIPS","JACKPOT",r.getString("mode"),r.getString("tier"),r.getLong("round_id")),user.id()));
 notices.sort(Comparator.comparingLong(Notice::createdAt));return notices;}
 @Transactional public void read(Accounts.User user,String id){if(db.queryForObject("SELECT COUNT(*) FROM color_jackpot_awards WHERE id=? AND player_id=?",Integer.class,id,user.id())==1){db.update("UPDATE color_jackpot_awards SET read_at=? WHERE id=? AND player_id=? AND read_at IS NULL",System.currentTimeMillis(),id,user.id());return;}if(db.queryForObject("SELECT COUNT(*) FROM lobby_daily_rewards WHERE id=? AND player_id=?",Integer.class,id,user.id())==1){db.update("UPDATE lobby_daily_rewards SET read_at=? WHERE id=? AND player_id=? AND read_at IS NULL",System.currentTimeMillis(),id,user.id());return;}if(db.queryForObject("SELECT COUNT(*) FROM chip_notifications WHERE id=? AND recipient_id=?",Integer.class,id,user.id())!=1)throw GameService.error(403,"FORBIDDEN");db.update("UPDATE chip_notifications SET read_at=? WHERE id=? AND recipient_id=? AND read_at IS NULL",System.currentTimeMillis(),id,user.id());}
}
