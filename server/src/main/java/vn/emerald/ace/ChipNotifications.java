package vn.emerald.ace;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service
public class ChipNotifications {
 private final JdbcTemplate db;
 public ChipNotifications(JdbcTemplate db){this.db=db;}
 public record Notice(String id,long amountCents,String senderName,long createdAt){}
 public List<Notice> unread(Accounts.User user){return db.query("SELECT n.*,a.display_name FROM chip_notifications n JOIN accounts a ON a.id=n.sender_id WHERE n.recipient_id=? AND n.read_at IS NULL ORDER BY n.created_at,n.id LIMIT 50",(r,n)->new Notice(r.getString("id"),r.getLong("amount_cents"),r.getString("display_name"),r.getLong("created_at")),user.id());}
 @Transactional public void read(Accounts.User user,String id){if(db.queryForObject("SELECT COUNT(*) FROM chip_notifications WHERE id=? AND recipient_id=?",Integer.class,id,user.id())!=1)throw GameService.error(403,"FORBIDDEN");db.update("UPDATE chip_notifications SET read_at=? WHERE id=? AND recipient_id=? AND read_at IS NULL",System.currentTimeMillis(),id,user.id());}
}
