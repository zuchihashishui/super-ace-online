package philip.emerald.ace.Utils;
import philip.emerald.ace.crash.CrashService;
import philip.emerald.ace.colorgame.ColorGameService;
import philip.emerald.ace.mines.MinesService;
import philip.emerald.ace.superace.GameEngine;
import philip.emerald.ace.superace.LobbyAutoService;
import philip.emerald.ace.superace.AutoService;
import philip.emerald.ace.superace.RtpSettings;
import philip.emerald.ace.dragontiger.DragonTigerService;
import philip.emerald.ace.dragontiger.DragonTigerEngine;
import philip.emerald.ace.superace.LobbyGameService;
import philip.emerald.ace.superace.GameService;
import philip.emerald.ace.colorgame.ColorGameEngine;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

@RestController @RequestMapping("/api")
public class ApiController {
 @org.springframework.beans.factory.annotation.Autowired AgentMembership memberships;
 public record RemovePlayerRequest(@NotNull UUID playerId,@NotNull UUID requestId){}
 @PostMapping("/agent-membership/remove") public Map<String,Boolean> removePlayer(HttpServletRequest r,@Valid @RequestBody RemovePlayerRequest b){memberships.remove(auth(r).user(),b.playerId().toString(),b.requestId().toString());return Map.of("ok",true);}
 public record JoinRequest(@NotNull @Pattern(regexp="[0-9]{6}") String agentCode){}
 @GetMapping("/agent-membership") public List<AgentMembership.Request> memberships(HttpServletRequest r){return memberships.list(auth(r).user());}
 @PostMapping("/agent-membership") public Map<String,Boolean> requestMembership(HttpServletRequest r,@Valid @RequestBody JoinRequest b){memberships.request(auth(r).user(),b.agentCode());return Map.of("ok",true);}
 @PostMapping("/agent-membership/decide") public Map<String,Boolean> decideMembership(HttpServletRequest r,@Valid @RequestBody DecisionRequest b){memberships.decide(auth(r).user(),b.id().toString(),b.approve());return Map.of("ok",true);}
 @org.springframework.beans.factory.annotation.Autowired ChipNotifications notifications;
 @GetMapping("/notifications") public List<ChipNotifications.Notice> notifications(HttpServletRequest r){return notifications.unread(auth(r).user());}
 @PostMapping("/notifications/{id}/read") public Map<String,Boolean> readNotification(HttpServletRequest r,@PathVariable UUID id){notifications.read(auth(r).user(),id.toString());return Map.of("ok",true);}
 @org.springframework.beans.factory.annotation.Autowired LobbyGameService lobby;
 @org.springframework.beans.factory.annotation.Autowired LobbyAutoService lobbyAuto;
 GameService gameFor(HttpServletRequest r,Accounts.Auth auth){String mode=r.getParameter("mode");if(mode==null)mode="LOBBY";if(!List.of("LOBBY","CLUB").contains(mode))throw GameService.error(400,"INVALID_MODE");return Accounts.canPlay(auth.user())&&mode.equals("LOBBY")?lobby:game;}
 AutoService autoFor(GameService selected){return selected==lobby?lobbyAuto:auto;}
 final GameService game;final Accounts accounts;final AutoService auto;final Reports reports;final Chips chips;final boolean secure;
 public ApiController(GameService game,Accounts accounts,AutoService auto,Reports reports,Chips chips,@Value("${ace.secure-cookie}")boolean secure){this.game=game;this.accounts=accounts;this.auto=auto;this.reports=reports;this.chips=chips;this.secure=secure;}
 String namedCookie(HttpServletRequest r,String name){if(r.getCookies()!=null)for(var c:r.getCookies())if(c.getName().equals(name))return c.getValue();return null;}
 String cookie(HttpServletRequest r){return namedCookie(r,"ACE_SESSION");}
 Accounts.Auth auth(HttpServletRequest r){var a=accounts.authenticate(cookie(r));if(!r.getMethod().equals("GET"))accounts.csrf(a,r.getHeader("X-CSRF-Token"));return a;}
 String sessionCookie(String token,long seconds){return ResponseCookie.from("ACE_SESSION",token).httpOnly(true).secure(secure).sameSite("Strict").path("/").maxAge(seconds).build().toString();}
 public record LoginRequest(@NotBlank @Size(max=40)String username,@NotBlank @Size(max=72)String password){}
 @PostMapping("/login") public ResponseEntity<?> login(HttpServletRequest r,@Valid @RequestBody LoginRequest body){var login=accounts.login(body.username(),body.password());return loggedIn(login,r);}
 ResponseEntity<?> loggedIn(Accounts.Login login,HttpServletRequest r){return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,sessionCookie(login.token(),900),ResponseCookie.from("ACE_REFRESH",login.refresh()).httpOnly(true).secure(secure).sameSite("Strict").path("/api").maxAge(604800).build().toString()).body(gameFor(r,login.auth()).me(login.auth()));}
 @org.springframework.beans.factory.annotation.Autowired DirectRegistration registration;
 public record RegisterRequest(@NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,40}")String username,@NotNull @Size(min=6,max=72)String password,@NotNull @Size(min=6,max=72)String rePassword){}
 @PostMapping("/register") public ResponseEntity<?> register(HttpServletRequest r,@Valid @RequestBody RegisterRequest b){if(!b.password().equals(b.rePassword()))throw GameService.error(400,"PASSWORD_MISMATCH");return loggedIn(registration.register(b.username(),b.password()),r);}
 @PostMapping("/refresh") public ResponseEntity<?> refresh(HttpServletRequest r){return loggedIn(accounts.refresh(namedCookie(r,"ACE_REFRESH")),r);}
 public record ChangeRequest(@NotNull Accounts.Role role,String parentId,boolean enabled){}
 @PutMapping("/accounts/{id}") public Accounts.User change(HttpServletRequest r,@PathVariable String id,@Valid @RequestBody ChangeRequest b){return accounts.change(auth(r).user(),id,b.role(),b.parentId(),b.enabled());}
 @org.springframework.beans.factory.annotation.Autowired HierarchyChips hierarchyChips;
 public record MoveRequest(@NotNull UUID requestId,@NotNull UUID targetId,@NotNull String direction,@Positive long amountCents){}
 @PostMapping("/chips/move") public Map<String,Object> move(HttpServletRequest r,@Valid @RequestBody MoveRequest b){return hierarchyChips.move(auth(r).user(),b.requestId().toString(),b.targetId().toString(),b.direction(),b.amountCents());}
 @PostMapping("/logout") public ResponseEntity<?> logout(HttpServletRequest r){auth(r);accounts.logout(cookie(r));return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,sessionCookie("",0),ResponseCookie.from("ACE_REFRESH","").httpOnly(true).secure(secure).sameSite("Strict").path("/api").maxAge(0).build().toString()).body(Map.of("ok",true));}
 @GetMapping("/me") public GameService.Me me(HttpServletRequest r){var a=auth(r);return gameFor(r,a).me(a);}
 public record PasswordRequest(@NotNull @Size(max=72)String oldPassword,@NotNull @Size(max=72)String newPassword){}
 @PostMapping("/password") public Map<String,Boolean> password(HttpServletRequest r,@Valid @RequestBody PasswordRequest b){accounts.password(auth(r),b.oldPassword(),b.newPassword());return Map.of("ok",true);}
 public record SpinRequest(@NotNull UUID requestId,@NotNull @Positive Long betCents,@NotNull @PositiveOrZero Long expectedRevision){}
 @PostMapping("/spins") public GameService.SpinResult spin(HttpServletRequest r,@Valid @RequestBody SpinRequest b){var a=auth(r);return gameFor(r,a).spin(a.user(),b.requestId().toString(),b.betCents(),b.expectedRevision());}
 public record AutoRequest(@NotNull UUID runId,@Min(10) @Max(500)int count,@Positive long betCents,@PositiveOrZero long expectedRevision,boolean turbo){}
 public record StopRequest(@NotNull UUID runId){}
 @GetMapping("/auto") public AutoService.State auto(HttpServletRequest r){var a=auth(r);return autoFor(gameFor(r,a)).state(a);}
 @PostMapping("/auto/start") public AutoService.Job start(HttpServletRequest r,@Valid @RequestBody AutoRequest b){var a=auth(r);return autoFor(gameFor(r,a)).start(a.user(),b.runId().toString(),b.count(),b.betCents(),b.expectedRevision(),b.turbo());}
 @PostMapping("/auto/stop") public AutoService.Job stop(HttpServletRequest r,@Valid @RequestBody StopRequest b){var a=auth(r);return autoFor(gameFor(r,a)).stop(a.user(),b.runId().toString());}
 @GetMapping("/accounts") public List<Accounts.AccountView> accounts(HttpServletRequest r){return accounts.overview(auth(r).user());}
 public record AccountRequest(@NotBlank @Size(max=40)String username,@NotBlank @Size(max=60)String displayName,@NotNull @Size(max=72)String password,@NotNull Accounts.Role role,@NotNull UUID parentId){}
 @PostMapping("/accounts") public Accounts.User create(HttpServletRequest r,@Valid @RequestBody AccountRequest b){return accounts.create(auth(r).user(),b.username(),b.displayName(),b.password(),b.role(),b.parentId().toString());}
 LocalDate date(String value){try{return value==null?reports.today():LocalDate.parse(value);}catch(Exception e){throw GameService.error(400,"INVALID_DATE");}}
 @GetMapping("/reports") public Reports.Report report(HttpServletRequest r,@RequestParam(defaultValue="week")String period,@RequestParam(required=false)String date){return reports.report(auth(r).user(),period,date(date));}
 @GetMapping("/ranking") public Map<String,Object> ranking(HttpServletRequest r,@RequestParam(defaultValue="week")String period,@RequestParam(required=false)String date){auth(r);return reports.ranking(period,date(date));}
 @GetMapping("/settings") public Reports.Settings settings(HttpServletRequest r){var u=auth(r).user();if(u.role()==Accounts.Role.PLAYER)throw GameService.error(403,"FORBIDDEN");return reports.settings();}
 public record SettingsRequest(@Min(0) @Max(10000)int commissionBps,@NotNull String commissionMode,@PositiveOrZero long revision){}
 @PostMapping("/settings") public Reports.Settings settings(HttpServletRequest r,@Valid @RequestBody SettingsRequest b){return reports.settings(auth(r).user(),b.commissionBps(),b.commissionMode(),b.revision());}
 @GetMapping("/settlements") public List<Reports.Settlement> settlements(HttpServletRequest r,@RequestParam(required=false)String date){return reports.settlements(auth(r).user(),date(date));}
 public record SettlementRequest(@NotNull UUID agentId,@NotNull String weekStart,@NotNull String action,@Min(0) @Max(10000)int rateBps,@PositiveOrZero long revision,@NotNull @Size(max=200)String note){}
 @PostMapping("/settlements/decide") public Map<String,Boolean> decide(HttpServletRequest r,@Valid @RequestBody SettlementRequest b){reports.decide(auth(r).user(),b.agentId().toString(),date(b.weekStart()),b.action(),b.rateBps(),b.revision(),b.note());return Map.of("ok",true);}
 @GetMapping("/chips") public List<Chips.Transfer> chips(HttpServletRequest r,@RequestParam(defaultValue="0")int page){if(page<0||page>100000)throw GameService.error(400,"INVALID_REQUEST");return chips.history(auth(r).user(),page);}
 public record ChipRequest(@NotNull UUID requestId,@NotNull String kind,@Positive long amountCents,@NotNull @Size(max=200)String reference){}
 @PostMapping("/chips") public Chips.Transfer chips(HttpServletRequest r,@Valid @RequestBody ChipRequest b){auth(r);throw GameService.error(403,"CHIP_REQUESTS_DISABLED");}
 public record DecisionRequest(@NotNull UUID id,boolean approve){}
 @PostMapping("/chips/decide") public Chips.Transfer decide(HttpServletRequest r,@Valid @RequestBody DecisionRequest b){auth(r);throw GameService.error(403,"CHIP_REQUESTS_DISABLED");}
 @org.springframework.beans.factory.annotation.Autowired RtpSettings rtpSettings;
 public record RtpRequest(@NotNull java.math.BigDecimal targetPercent,@PositiveOrZero long revision){}
 @PutMapping("/rtp") public Map<String,Object> updateRtp(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody RtpRequest body){rtpSettings.update(auth(r).user(),mode,body.targetPercent(),body.revision());return rtp(r,mode);}
 @GetMapping("/rtp") public Map<String,Object> rtp(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){
  accounts.require(auth(r).user(),Accounts.Role.CREATOR);
  var setting=rtpSettings.get(mode);var selected=mode.equals("LOBBY")?lobby:game;
  var totals=game.db().queryForMap("SELECT COALESCE(SUM(wager_cents),0) AS wager,COALESCE(SUM(payout_cents),0) AS payout FROM "+selected.table("round_ledger")+" WHERE rtp_profile=?",setting.profile());
  long wager=((Number)totals.get("wager")).longValue(),payout=((Number)totals.get("payout")).longValue();
  var info=new HashMap<String,Object>();info.put("mode",mode);info.put("targetPercent",setting.targetPercent());info.put("revision",setting.revision());info.put("payoutScale",GameEngine.scaleFor(setting.profile()));info.put("observedPercent",wager==0?null:100.0*payout/wager);return info;
 }
 @org.springframework.beans.factory.annotation.Autowired DragonTigerService dragonTiger;
 public record DragonTigerRequest(@NotNull UUID requestId,@PositiveOrZero long tableRoundId,@NotNull DragonTigerEngine.Side side,@Positive long betCents,@PositiveOrZero long expectedRevision){}
 @GetMapping("/dragon-tiger/table") public DragonTigerService.Table dragonTigerTable(){return dragonTiger.table();}
 @GetMapping("/dragon-tiger/crowd") public DragonTigerService.Crowd dragonTigerCrowd(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@RequestParam long roundId){return dragonTiger.crowd(cookie(r)==null?null:auth(r).user(),mode,roundId);}
 @GetMapping("/dragon-tiger/table/history") public List<DragonTigerService.TableResult> dragonTigerTableHistory(){return dragonTiger.tableHistory();}
 @GetMapping("/dragon-tiger/bets") public List<DragonTigerService.Round> dragonTigerBets(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@RequestParam long roundId){return dragonTiger.bets(auth(r).user(),mode,roundId);}
 @PostMapping("/dragon-tiger/rounds") public DragonTigerService.Round dragonTiger(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody DragonTigerRequest b){return dragonTiger.play(auth(r).user(),mode,b.tableRoundId(),b.requestId().toString(),b.side(),b.betCents(),b.expectedRevision());}
 @GetMapping("/dragon-tiger/rounds") public List<DragonTigerService.Round> dragonTigerHistory(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){return dragonTiger.history(auth(r).user(),mode);}
 @org.springframework.beans.factory.annotation.Autowired philip.emerald.ace.colorgame.ColorJackpotService colorJackpot;
 @GetMapping("/color-game/jackpot") public Object colorJackpot(@RequestParam(defaultValue="LOBBY")String mode){return colorJackpot.snapshot(mode);}
 @GetMapping("/color-game/jackpot/awards") public Object colorJackpotAwards(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){return colorJackpot.awards(auth(r).user(),mode);}
 @GetMapping("/color-game/jackpot/history") public Object colorJackpotHistory(@RequestParam(defaultValue="LOBBY")String mode){return colorJackpot.history(mode);}
 public record JackpotTopUp(@NotNull UUID requestId,@Positive long amountCents){}
 @PostMapping("/color-game/jackpot/top-up") public Object colorJackpotTopUp(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody JackpotTopUp b){return colorJackpot.topUp(auth(r).user(),mode,b.requestId().toString(),b.amountCents());}
 @org.springframework.beans.factory.annotation.Autowired ColorGameService colorGame;
 public record ColorGameRequest(@NotNull UUID requestId,@PositiveOrZero long tableRoundId,@NotNull ColorGameEngine.Side side,@Positive long betCents,@PositiveOrZero long expectedRevision){}
 @GetMapping("/color-game/table") public ColorGameService.Table colorGameTable(){return colorGame.table();}
 @GetMapping("/color-game/crowd") public ColorGameService.Crowd colorGameCrowd(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@RequestParam long roundId){return colorGame.crowd(cookie(r)==null?null:auth(r).user(),mode,roundId);}
 @GetMapping("/color-game/table/history") public List<ColorGameService.TableResult> colorGameTableHistory(){return colorGame.tableHistory();}
 @GetMapping("/color-game/bets") public List<ColorGameService.Round> colorGameBets(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@RequestParam long roundId){return colorGame.bets(auth(r).user(),mode,roundId);}
 @PostMapping("/color-game/rounds") public ColorGameService.Round colorGame(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody ColorGameRequest b){return colorGame.play(auth(r).user(),mode,b.tableRoundId(),b.requestId().toString(),b.side(),b.betCents(),b.expectedRevision());}
 @GetMapping("/color-game/rounds") public List<ColorGameService.Round> colorGameHistory(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){return colorGame.history(auth(r).user(),mode);}
 @GetMapping("/color-game/winners") public List<ColorGameService.Winner> colorWinners(@RequestParam(defaultValue="LOBBY")String mode){return colorGame.winners(mode);}
 @org.springframework.beans.factory.annotation.Autowired CrashService crash;
 public record CrashBetRequest(@NotNull UUID requestId,@Positive long tableRoundId,@Positive long betCents,@Min(0) @Max(9999) int autoCashoutBps,@PositiveOrZero long expectedRevision){}
 public record CashoutRequest(@NotNull UUID requestId){}
 @GetMapping("/crash/table") public CrashService.Table crashTable(@RequestParam(defaultValue="LOBBY")String mode){return crash.table(mode);}
 @GetMapping("/crash/history") public List<CrashService.Result> crashHistory(){return crash.history();}
 @GetMapping("/crash/bets") public List<CrashService.Bet> crashBets(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){return crash.bets(auth(r).user(),mode);}
 @PostMapping("/crash/bets") public CrashService.Bet crashBet(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody CrashBetRequest b){return crash.bet(auth(r).user(),mode,b.tableRoundId(),b.requestId().toString(),b.betCents(),b.autoCashoutBps(),b.expectedRevision());}
 @PostMapping("/crash/cashout") public CrashService.Bet crashCashout(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody CashoutRequest b){return crash.cashout(auth(r).user(),mode,b.requestId().toString());}
 @org.springframework.beans.factory.annotation.Autowired ArcadeService arcade;
 public record ArcadeRequest(@NotNull UUID requestId,@Positive long betCents,@PositiveOrZero long expectedRevision,@Min(0) @Max(19) int selection){}
 @GetMapping("/arcade/{kind}/rounds") public List<ArcadeService.Round> arcadeHistory(HttpServletRequest r,@PathVariable ArcadeEngine.Game kind,@RequestParam(defaultValue="LOBBY")String mode){return arcade.history(auth(r).user(),mode,kind);}
 @PostMapping("/arcade/{kind}/rounds") public ArcadeService.Round arcadePlay(HttpServletRequest r,@PathVariable ArcadeEngine.Game kind,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody ArcadeRequest b){return arcade.play(auth(r).user(),mode,kind,b.requestId().toString(),b.betCents(),b.expectedRevision(),b.selection());}
 @GetMapping("/arcade/plinko/rules") public Map<String,Object> plinkoRules(){return Map.of("rows",12,"multipliersBps",ArcadeEngine.PLINKO,"minBetCents",GameEngine.MIN_BET,"maxBetCents",GameEngine.MAX_BET);}
 @GetMapping("/arcade/{kind}/rules") public Map<String,Object> arcadeRules(@PathVariable ArcadeEngine.Game kind){return Map.of("rows",kind==ArcadeEngine.Game.PLINKO?12:0,"multipliersBps",kind==ArcadeEngine.Game.PLINKO?ArcadeEngine.PLINKO:kind==ArcadeEngine.Game.WHEEL?ArcadeEngine.WHEEL:kind==ArcadeEngine.Game.SAKLA?List.of(1900):kind==ArcadeEngine.Game.BINGO?List.of(675):List.of(500,1000,2500,10000,300),"minBetCents",GameEngine.MIN_BET,"maxBetCents",GameEngine.MAX_BET);}
 @org.springframework.beans.factory.annotation.Autowired MinesService mines;
 public record MinesStart(@NotNull UUID requestId,@Positive long betCents,@Min(1) @Max(10) int mineCount,@PositiveOrZero long expectedRevision){}
 public record MinesMove(@NotNull UUID requestId,@Min(0) @Max(24) Integer tile){}
 @GetMapping("/mines/rounds") public List<MinesService.Round> minesHistory(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){return mines.history(auth(r).user(),mode);}
 @PostMapping("/mines/start") public MinesService.Round minesStart(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody MinesStart b){return mines.start(auth(r).user(),mode,b.requestId().toString(),b.betCents(),b.mineCount(),b.expectedRevision());}
 @PostMapping("/mines/move") public MinesService.Round minesMove(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody MinesMove b){return mines.move(auth(r).user(),mode,b.requestId().toString(),b.tile());}
 @GetMapping("/health") public ResponseEntity<?> health(){boolean ready=game.db().queryForObject("SELECT COUNT(*) FROM accounts WHERE role='CREATOR'",Integer.class)>0;return ResponseEntity.status(ready?200:503).body(Map.of("status",ready?"up":"starting","version","13.0.0"));}

 @org.springframework.beans.factory.annotation.Autowired philip.emerald.ace.luckynine.LuckyNineService luckyNine;
 public record LuckyNineStart(@NotNull UUID requestId,@Positive long betCents,@PositiveOrZero long expectedRevision){}
 public record LuckyNineMove(@NotNull UUID requestId,@NotNull philip.emerald.ace.luckynine.LuckyNineEngine.Action action){}
 @GetMapping("/lucky-nine/rounds") public List<philip.emerald.ace.luckynine.LuckyNineService.Round> luckyNineHistory(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode){return luckyNine.history(auth(r).user(),mode);}
 @PostMapping("/lucky-nine/start") public philip.emerald.ace.luckynine.LuckyNineService.Round luckyNineStart(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody LuckyNineStart b){return luckyNine.start(auth(r).user(),mode,b.requestId().toString(),b.betCents(),b.expectedRevision());}
 @PostMapping("/lucky-nine/move") public philip.emerald.ace.luckynine.LuckyNineService.Round luckyNineMove(HttpServletRequest r,@RequestParam(defaultValue="LOBBY")String mode,@Valid @RequestBody LuckyNineMove b){return luckyNine.move(auth(r).user(),mode,b.requestId().toString(),b.action());}
}
