package vn.emerald.ace;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Same rules and transaction boundaries; physically isolated Gold accounting. */
@Service
public class LobbyGameService extends GameService {
 public LobbyGameService(JdbcTemplate db,ObjectMapper json,Accounts accounts,@Value("${ace.spin-cooldown-ms}")long cooldown){super(db,json,accounts,cooldown);}
 @Override String table(String name){return "lobby_"+name;}
 @Override String mode(){return "LOBBY";}
 @Override String activeProfile(){return "LOBBY_98";}
}
