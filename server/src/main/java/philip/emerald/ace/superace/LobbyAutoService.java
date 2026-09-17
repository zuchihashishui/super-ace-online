package philip.emerald.ace.superace;
import philip.emerald.ace.Utils.Accounts;
import org.springframework.stereotype.Service;
@Service
public class LobbyAutoService extends AutoService {
 public LobbyAutoService(LobbyGameService game,Accounts accounts){super(game,accounts);}
}
