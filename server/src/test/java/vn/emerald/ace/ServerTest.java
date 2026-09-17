package vn.emerald.ace;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import jakarta.servlet.http.Cookie;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=${TEST_DB_URL:jdbc:h2:mem:test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1}","spring.datasource.username=${TEST_DB_USER:sa}","spring.datasource.password=${TEST_DB_PASSWORD:}","ace.creator-user=creator","ace.zone=Asia/Manila","ace.creator-password=test-only-creator-password","ace.spin-cooldown-ms=0","ace.jobs-enabled=false","ace.jwt-secret=test-only-secret-at-least-32-characters"})
@AutoConfigureMockMvc
class ServerTest {

 @Autowired DailyLobbyGold dailyGold;
 @Autowired ChipNotifications chipNotices;
 @Test void dailyLobbyRewardIsAdditiveIsolatedAndAcknowledged() throws Exception {
  var u=tree().player();var other=tree().player();var day=LocalDate.of(2030,1,2);
  long before=lobby.wallet(u.id(),false).balanceCents(),club=game.wallet(u.id(),false).balanceCents();
  try(var pool=Executors.newFixedThreadPool(4)){
   var futures=new ArrayList<Future<?>>();for(int i=0;i<8;i++)futures.add(pool.submit(()->dailyGold.grant(u.id(),day)));
   for(var f:futures)f.get();
  }
  assertEquals(before+1000000,lobby.wallet(u.id(),false).balanceCents());
  assertEquals(club,game.wallet(u.id(),false).balanceCents());
  var notices=chipNotices.unread(u);assertEquals(1,notices.size());assertEquals("GOLD",notices.getFirst().currency());
  assertThrows(GameService.ApiError.class,()->chipNotices.read(other,notices.getFirst().id()));
  chipNotices.read(u,notices.getFirst().id());chipNotices.read(u,notices.getFirst().id());assertTrue(chipNotices.unread(u).isEmpty());
  dailyGold.grant(u.id(),day);assertEquals(before+1000000,lobby.wallet(u.id(),false).balanceCents());
  dailyGold.grant(u.id(),day.plusDays(1));assertEquals(before+2000000,lobby.wallet(u.id(),false).balanceCents());assertEquals(1,chipNotices.unread(u).size());
 }
 @Autowired DragonTigerService dragonTiger;
 @Test void legacyUnsettledRoundKeepsTwoCardsAndNewRoundsUseOne(){
  var u=tree().player();long round=123456L,close=round*DragonTigerService.CYCLE_MS+DragonTigerService.BETTING_MS;
  var row=new DragonTigerService.Round(id(),"LOBBY",round,close,DragonTigerEngine.Side.DRAGON,500,0,1000000,999500,1,close-1000,null);
  db.update("INSERT INTO lobby_round_ledger(player_id,request_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type,game_round_id,dt_settled) VALUES(?,?,500,500,0,FALSE,1,?,?,'DT_TWO_CARD_V2','DRAGON_TIGER',?,FALSE)",u.id(),row.requestId(),lobby.encode(row),row.createdAt(),round);
  dragonTiger.settle("LOBBY",u.id(),close);var old=dragonTiger.historyAt(u,"LOBBY",close).getFirst();assertEquals(2,old.outcome().dragon().size());assertEquals(2,old.outcome().tiger().size());
  var modern=dragonTiger.tableAt((round+1)*DragonTigerService.CYCLE_MS+DragonTigerService.BETTING_MS).outcome();assertEquals(1,modern.dragon().size());assertEquals(1,modern.tiger().size());
  assertEquals(old.outcome(),dragonTiger.tableAt(close).outcome());
 }

 @Test void dragonTigerCrowdExcludesViewerAndSeparatesCurrencies()throws Exception{
  var a=tree().player();var b=tree().player();long round=bettingRound();
  var first=dragonTiger.play(a,"LOBBY",round,id(),DragonTigerEngine.Side.DRAGON,500,0);
  dragonTiger.play(a,"LOBBY",round,id(),DragonTigerEngine.Side.DRAGON,500,1);
  dragonTiger.play(b,"LOBBY",round,id(),DragonTigerEngine.Side.DRAGON,2000,0);
  dragonTiger.play(b,"LOBBY",round,id(),DragonTigerEngine.Side.TIGER,1000,1);
  dragonTiger.play(a,"LOBBY",round,first.requestId(),first.side(),500,0);
  var mine=dragonTiger.crowd(a,"LOBBY",round);var d=mine.sides().stream().filter(s->s.side()==DragonTigerEngine.Side.DRAGON).findFirst().orElseThrow();
  assertEquals(1000,d.ownCents());assertEquals(2,d.ownChipCount());assertTrue(d.othersCents()>=2000);assertTrue(d.otherBettors()>=1);
  var theirs=dragonTiger.crowd(b,"LOBBY",round).sides().stream().filter(s->s.side()==DragonTigerEngine.Side.DRAGON).findFirst().orElseThrow();assertEquals(2000,theirs.ownCents());assertEquals(1,theirs.ownChipCount());assertEquals(d.othersCents()+d.ownCents(),theirs.othersCents()+theirs.ownCents());
  dragonTiger.play(b,"CLUB",round,id(),DragonTigerEngine.Side.DRAGON,1500,0);
  var club=dragonTiger.crowd(a,"CLUB",round);assertTrue(club.sides().stream().allMatch(s->s.ownCents()==0));assertTrue(club.sides().stream().filter(s->s.side()==DragonTigerEngine.Side.DRAGON).findFirst().orElseThrow().othersCents()>=1500);
  assertEquals(d.othersCents(),dragonTiger.crowd(a,"LOBBY",round).sides().stream().filter(s->s.side()==DragonTigerEngine.Side.DRAGON).findFirst().orElseThrow().othersCents());
  var guest=dragonTiger.crowd(null,"LOBBY",round);assertTrue(guest.sides().stream().allMatch(s->s.ownCents()==0));assertTrue(guest.bettors()>=2);
  String json=mvc.perform(get("/api/dragon-tiger/crowd").param("mode","LOBBY").param("roundId",String.valueOf(round))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
  assertFalse(json.contains(a.id()));assertFalse(json.contains(b.id()));assertFalse(json.contains("outcome"));assertFalse(json.contains("payout"));
  assertThrows(GameService.ApiError.class,()->dragonTiger.crowd(a,"BAD",round));assertThrows(GameService.ApiError.class,()->dragonTiger.crowd(a,"LOBBY",round+100));
 }
 @Test void dragonTigerHistoryLimitsGamesNotChipClicks()throws Exception{
  var u=tree().player();long current=System.currentTimeMillis()/DragonTigerService.CYCLE_MS,rev=0;
  for(int i=0;i<22;i++){
   long round=current-100+i,close=round*DragonTigerService.CYCLE_MS+DragonTigerService.BETTING_MS;var outcome=dragonTiger.tableAt(close).outcome();
   for(int j=0;j<3;j++){
    var row=new DragonTigerService.Round(id(),"LOBBY",round,close,DragonTigerEngine.Side.DRAGON,500,0,10000,9500,++rev,close-1000,outcome);
    db.update("INSERT INTO lobby_round_ledger(player_id,request_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at,rtp_profile,game_type,game_round_id) VALUES(?,?,500,500,0,FALSE,?,?,?,'DT_TWO_CARD_V2','DRAGON_TIGER',?)",u.id(),row.requestId(),rev,lobby.encode(row),row.createdAt(),round);
   }
  }
  var rows=dragonTiger.history(u,"LOBBY");assertEquals(60,rows.size());assertEquals(20,rows.stream().map(DragonTigerService.Round::tableRoundId).distinct().count());assertEquals(current-79,rows.getFirst().tableRoundId());assertEquals(current-98,rows.getLast().tableRoundId());
  dragonTiger.tableAt((current+100)*DragonTigerService.CYCLE_MS+DragonTigerService.BETTING_MS);
  var publicRows=dragonTiger.tableHistory();assertEquals(20,publicRows.size());assertTrue(publicRows.stream().allMatch(r->r.createdAt()<=System.currentTimeMillis()));
  mvc.perform(get("/api/dragon-tiger/table/history")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(20));
 }
 @Test void dragonTigerMultipleChipsReserveAndSettleExactlyOnce(){
  var u=tree().player();long round=bettingRound(),before=lobby.wallet(u.id(),false).balanceCents(),clubBefore=game.wallet(u.id(),false).balanceCents();
  var first=dragonTiger.play(u,"LOBBY",round,id(),DragonTigerEngine.Side.DRAGON,1000,0);
  var second=dragonTiger.play(u,"LOBBY",round,id(),DragonTigerEngine.Side.DRAGON,1000,1);
  var third=dragonTiger.play(u,"LOBBY",round,id(),DragonTigerEngine.Side.TIE,500,2);
  assertEquals(before-2500,lobby.wallet(u.id(),false).balanceCents());assertEquals(clubBefore,game.wallet(u.id(),false).balanceCents());
  for(var bet:List.of(first,second,third)){assertNull(bet.outcome());assertEquals(0,bet.payoutCents());}
  assertEquals(3,dragonTiger.bets(u,"LOBBY",round).size());
  assertEquals(first,dragonTiger.play(u,"LOBBY",round,first.requestId(),first.side(),first.betCents(),0));
  assertEquals(before-2500,lobby.wallet(u.id(),false).balanceCents());
  dragonTiger.settle("LOBBY",u.id(),third.revealAt()-1);assertEquals(before-2500,lobby.wallet(u.id(),false).balanceCents());
  dragonTiger.settle("LOBBY",u.id(),third.revealAt());
  var settled=dragonTiger.historyAt(u,"LOBBY",third.revealAt());assertEquals(3,settled.size());long payout=0;
  for(var bet:settled){assertNotNull(bet.outcome());assertEquals(DragonTigerEngine.payout(bet.betCents(),bet.side(),bet.outcome().winner()),bet.payoutCents());assertEquals(settled.getFirst().outcome(),bet.outcome());payout+=bet.payoutCents();}
  long after=before-2500+payout;assertEquals(after,lobby.wallet(u.id(),false).balanceCents());
  dragonTiger.settle("LOBBY",u.id(),third.revealAt()+100);assertEquals(after,lobby.wallet(u.id(),false).balanceCents());
 }
 @Test void dragonTigerNoOverspendAndNoLateBets(){
  var u=tree().player();db.update("UPDATE wallets SET balance=1500 WHERE player_id=?",u.id());long round=bettingRound();
  dragonTiger.play(u,"CLUB",round,id(),DragonTigerEngine.Side.DRAGON,1000,0);
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",round,id(),DragonTigerEngine.Side.TIGER,1000,1));
  assertEquals(500,game.wallet(u.id(),false).balanceCents());
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",round-1,id(),DragonTigerEngine.Side.TIE,500,1));
  assertEquals(500,game.wallet(u.id(),false).balanceCents());
 }
 @Test void dragonTigerTableHasTenSecondBettingAndFiveSecondReveal(){long id=12345,start=id*DragonTigerService.CYCLE_MS;var betting=dragonTiger.tableAt(start+1);assertEquals("BETTING",betting.phase());assertEquals(10,betting.secondsRemaining());assertNull(betting.outcome());var reveal=dragonTiger.tableAt(start+DragonTigerService.BETTING_MS);assertEquals("REVEAL",reveal.phase());assertEquals(5,reveal.secondsRemaining());assertNotNull(reveal.outcome());assertEquals(id,reveal.roundId());assertEquals(start+DragonTigerService.CYCLE_MS,reveal.nextRoundAt());}
 @Test void dragonTigerWalletIsolationRetryAndReports(){
  var t=tree();var u=t.player();long clubBefore=game.wallet(u.id(),false).balanceCents(),goldBefore=lobby.wallet(u.id(),false).balanceCents();
  String request=id();var gold=dragonTiger.play(u,"LOBBY",bettingRound(),request,DragonTigerEngine.Side.TIE,755,0);
  assertEquals(goldBefore-755+gold.payoutCents(),lobby.wallet(u.id(),false).balanceCents());
  assertEquals(clubBefore,game.wallet(u.id(),false).balanceCents());
  assertEquals(gold,dragonTiger.play(u,"LOBBY",bettingRound(),request,DragonTigerEngine.Side.TIE,755,0));
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"LOBBY",bettingRound(),request,DragonTigerEngine.Side.DRAGON,755,0));
  assertThrows(GameService.ApiError.class,()->lobby.spin(u,request,755,gold.revision()));
  var result=dragonTiger.play(u,"CLUB",bettingRound(),request,DragonTigerEngine.Side.DRAGON,500,0);
  assertEquals(clubBefore-500+result.payoutCents(),game.wallet(u.id(),false).balanceCents());
  assertEquals(gold.balanceCents(),lobby.wallet(u.id(),false).balanceCents());
  assertEquals(0,dragonTiger.history(u,"CLUB").size());long after=result.revealAt()+1;assertEquals(1,dragonTiger.historyAt(u,"CLUB",after).size());assertEquals(1,dragonTiger.historyAt(u,"LOBBY",after).size());
  var settled=dragonTiger.historyAt(u,"CLUB",after).getFirst();var report=reports.report(u,"day",reports.today());assertEquals(500,report.wagerCents());assertEquals(settled.payoutCents(),report.payoutCents());
  assertEquals("DRAGON_TIGER",db.queryForObject("SELECT game_type FROM round_ledger WHERE player_id=?",String.class,u.id()));
 }
 @Test void dragonTigerGuardsAndSharedWalletConcurrency()throws Exception{
  var t=tree();var u=t.player();
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"BAD",bettingRound(),id(),DragonTigerEngine.Side.TIE,500,0));
  for(long invalid:List.of(0L,499L,50001L))assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",bettingRound(),id(),DragonTigerEngine.Side.TIE,invalid,0));
  db.update("UPDATE wallets SET free_spins=1,locked_bet=2000 WHERE player_id=?",u.id());
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",bettingRound(),id(),DragonTigerEngine.Side.TIE,500,0));
  db.update("UPDATE wallets SET free_spins=0,locked_bet=0 WHERE player_id=?",u.id());
  String run=id();auto.start(u,run,10,500,0,false);
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",bettingRound(),id(),DragonTigerEngine.Side.TIE,500,0));
  auto.stop(u,run);
  String request=id();
  try(var pool=Executors.newFixedThreadPool(4)){
   var tasks=new ArrayList<Callable<DragonTigerService.Round>>();for(int i=0;i<4;i++)tasks.add(()->dragonTiger.play(u,"CLUB",bettingRound(),request,DragonTigerEngine.Side.TIGER,500,0));
   var results=pool.invokeAll(tasks);var first=results.getFirst().get();for(var result:results)assertEquals(first,result.get());
  }
  assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM round_ledger WHERE player_id=?",Integer.class,u.id()));
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",bettingRound(),id(),DragonTigerEngine.Side.TIE,500,0));
  db.update("UPDATE wallets SET balance=0 WHERE player_id=?",u.id());
  assertThrows(GameService.ApiError.class,()->dragonTiger.play(u,"CLUB",bettingRound(),id(),DragonTigerEngine.Side.TIE,500,1));
 }
 @Test void dragonTigerEndpointsRequireAuthAndCsrf()throws Exception{
  mvc.perform(get("/api/dragon-tiger/table")).andExpect(status().isOk()).andExpect(jsonPath("$.secondsRemaining").isNumber());
  mvc.perform(get("/api/dragon-tiger/rounds")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/dragon-tiger/rounds").header("X-Game-Client","web").contentType(MediaType.APPLICATION_JSON).content("{\"requestId\":\""+id()+"\",\"tableRoundId\":"+bettingRound()+",\"side\":\"DRAGON\",\"betCents\":500,\"expectedRevision\":0}")).andExpect(status().isUnauthorized());
  var t=tree();var login=accounts.login(t.player().username(),"test-only-user-password");var cookie=new Cookie("ACE_SESSION",login.token());
  mvc.perform(post("/api/dragon-tiger/rounds").cookie(cookie).header("X-Game-Client","web").contentType(MediaType.APPLICATION_JSON).content("{\"requestId\":\""+id()+"\",\"tableRoundId\":"+bettingRound()+",\"side\":\"DRAGON\",\"betCents\":500,\"expectedRevision\":0}")).andExpect(status().isForbidden());
 }
 long bettingRound(){for(int i=0;i<120;i++){var table=dragonTiger.table();if(table.phase().equals("BETTING")&&table.secondsRemaining()>1)return table.roundId();try{Thread.sleep(50);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException(e);}}throw new AssertionError("No Dragon Tiger betting window");}
 @Autowired AgentMembership memberships;
 @Autowired HierarchyChips hierarchyChips;
 @Autowired LobbyGameService lobby;
 @Autowired LobbyAutoService lobbyAuto;
 @Autowired RtpSettings rtpSettings;
 @Test void onlyCreatorCanReadAndUpdateDecimalRtp()throws Exception{
  var t=tree();var old=rtpSettings.get("CLUB");
  mvc.perform(get("/api/rtp")).andExpect(status().isUnauthorized());
  for(var user:List.of(t.player(),t.agent(),t.sa())){
   var login=accounts.login(user.username(),"test-only-user-password");var cookie=new Cookie("ACE_SESSION",login.token());
   mvc.perform(get("/api/rtp").cookie(cookie)).andExpect(status().isForbidden());
   mvc.perform(put("/api/rtp").cookie(cookie).header("X-Game-Client","web").header("X-CSRF-Token",login.auth().csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"targetPercent\":97.55,\"revision\":0}")).andExpect(status().isForbidden());
   assertNull(game.me(login.auth()).rtpProfile());
  }
  var login=accounts.login(t.root().username(),"test-only-creator-password");var cookie=new Cookie("ACE_SESSION",login.token());
  try{
   mvc.perform(put("/api/rtp?mode=CLUB").cookie(cookie).header("X-Game-Client","web").header("X-CSRF-Token",login.auth().csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"targetPercent\":97.55,\"revision\":"+old.revision()+"}")).andExpect(status().isOk()).andExpect(jsonPath("$.targetPercent").value(97.55));
   assertEquals("RTP_9755",game.activeProfile());assertEquals("RTP_10000",lobby.activeProfile());
   assertThrows(GameService.ApiError.class,()->rtpSettings.update(t.root(),"CLUB",new java.math.BigDecimal("98"),old.revision()));
   for(String invalid:List.of("0","100.01","97.555"))assertThrows(GameService.ApiError.class,()->rtpSettings.update(t.root(),"CLUB",new java.math.BigDecimal(invalid),rtpSettings.get("CLUB").revision()));
   var result=game.spin(t.player(),id(),2000,0);
   assertEquals("RTP_9755",db.queryForObject("SELECT rtp_profile FROM round_ledger WHERE player_id=?",String.class,t.player().id()));
   db.update("UPDATE wallets SET free_spins=1,locked_bet=2000,bonus_profile='RTP_9755' WHERE player_id=?",t.player().id());
   rtpSettings.update(t.root(),"CLUB",new java.math.BigDecimal("96"),rtpSettings.get("CLUB").revision());
   String freeId=id();game.spin(t.player(),freeId,2000,result.revision());
   assertEquals("RTP_9755",db.queryForObject("SELECT rtp_profile FROM round_ledger WHERE player_id=? AND request_id=?",String.class,t.player().id(),freeId));
  }finally{rtpSettings.update(t.root(),"CLUB",old.targetPercent(),rtpSettings.get("CLUB").revision());}
 }
 @Test void everyRoleCanPlayBothModesAndAutoplay(){
  var t=tree();
  for(var user:List.of(t.player(),t.agent(),t.sa(),t.root())){
   db.update("UPDATE wallets SET balance=1000000 WHERE player_id=?",user.id());
   db.update("UPDATE lobby_wallets SET balance=1000000 WHERE player_id=?",user.id());
   var result=game.spin(user,id(),2000,game.wallet(user.id(),false).revision());
   assertEquals(1000000,lobby.wallet(user.id(),false).balanceCents());
   var gold=lobby.spin(user,id(),2000,lobby.wallet(user.id(),false).revision());
   assertEquals(result.balanceCents(),game.wallet(user.id(),false).balanceCents());
   String run=id();auto.start(user,run,10,2000,result.revision(),true);auto.process(user.id());auto.stop(user,run);
   assertEquals(result.revision()+1,game.wallet(user.id(),false).revision());
   String lobbyRun=id();lobbyAuto.start(user,lobbyRun,10,2000,gold.revision(),true);lobbyAuto.process(user.id());lobbyAuto.stop(user,lobbyRun);
   assertEquals(gold.revision()+1,lobby.wallet(user.id(),false).revision());
   if(user.role()==Accounts.Role.CREATOR||user.role()==Accounts.Role.SUPER_AGENT)assertEquals(0,db.queryForObject("SELECT COUNT(*) FROM round_ledger WHERE player_id=? AND agent_id IS NOT NULL",Integer.class,user.id()));
  }
 }
 @Test void defaultAgentCanRemovePlayerWhoKeepsClubAndCanPlay(){
  var root=creator();var agent=accounts.user(db.queryForObject("SELECT agent_id FROM direct_registration WHERE id=1",String.class));
  var player=create(root,agent,Accounts.Role.PLAYER);String pid=player.id();
  db.update("UPDATE wallets SET balance=100000 WHERE player_id=?",pid);
  var club=db.queryForObject("SELECT club_id FROM accounts WHERE id=?",String.class,pid);
  memberships.remove(agent,pid,id());var removed=accounts.user(pid);
  assertNull(removed.parentId());assertEquals(club,db.queryForObject("SELECT club_id FROM accounts WHERE id=?",String.class,pid));
  assertEquals(100000,game.wallet(pid,false).balanceCents());
  assertFalse(accounts.inScope(agent,removed));assertFalse(accounts.inScope(accounts.user(agent.parentId()),removed));
  assertThrows(GameService.ApiError.class,()->hierarchyChips.move(agent,id(),pid,"TAKE",100));
  game.spin(removed,id(),2000,0);lobby.spin(removed,id(),2000,0);
  assertNull(db.queryForObject("SELECT agent_id FROM round_ledger WHERE player_id=?",String.class,pid));
  assertNull(db.queryForObject("SELECT agent_id FROM lobby_round_ledger WHERE player_id=?",String.class,pid));
  var report=reports.report(removed,"week",LocalDate.now(ZoneId.of("Asia/Manila")));
  assertEquals(2000,report.wagerCents());
  memberships.request(removed,agent.publicCode());
  var pending=memberships.list(agent).stream().filter(r->r.playerId().equals(pid)).findFirst().orElseThrow();
  assertEquals("PENDING",pending.status());
  assertNull(accounts.user(pid).parentId());
  assertFalse(accounts.inScope(agent,accounts.user(pid)));
  assertThrows(GameService.ApiError.class,()->hierarchyChips.move(agent,id(),pid,"TAKE",100));
  String pendingId=pending.id();
  assertThrows(GameService.ApiError.class,()->memberships.decide(removed,pendingId,true));
  memberships.decide(agent,pending.id(),false);
  assertNull(accounts.user(pid).parentId());
  assertEquals("REJECTED",memberships.list(removed).getFirst().status());
  memberships.request(removed,agent.publicCode());
  pending=memberships.list(agent).stream().filter(r->r.playerId().equals(pid)).findFirst().orElseThrow();
  memberships.decide(agent,pending.id(),true);assertEquals(agent.id(),accounts.user(pid).parentId());
 }
 @Test void removePlayerPreservesWalletAndSessionAndRevokesScope(){
  var t=tree();var other=tree();String target=t.player().id(),request=id();
  var login=accounts.login(t.player().username(),"test-only-user-password");
  db.update("UPDATE wallets SET free_spins=2,locked_bet=2000 WHERE player_id=?",target);
  auto.start(t.player(),id(),10,2000,0,true);
  memberships.request(t.player(),other.agent().publicCode());
  var before=game.wallet(target,false);var gold=lobby.wallet(target,false);var job=auto.job(target);
  assertThrows(GameService.ApiError.class,()->memberships.remove(other.agent(),target,id()));
  assertThrows(GameService.ApiError.class,()->memberships.remove(t.player(),target,id()));
  memberships.remove(t.agent(),target,request);
  assertNull(accounts.user(target).parentId());
  assertEquals(before,game.wallet(target,false));assertEquals(gold,lobby.wallet(target,false));
  assertEquals(job,auto.job(target));assertTrue(accounts.user(target).enabled());
  assertNull(accounts.authenticate(login.token()).user().parentId());
  assertFalse(accounts.inScope(t.agent(),accounts.user(target)));
  assertEquals("REMOVED",memberships.list(accounts.user(target)).getFirst().status());
  assertTrue(memberships.list(other.agent()).isEmpty());
  assertThrows(GameService.ApiError.class,()->hierarchyChips.move(t.agent(),id(),target,"TAKE",100));
  memberships.remove(t.agent(),target,request);
  assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM audit_log WHERE id=?",Integer.class,request));
  memberships.request(accounts.user(target),t.agent().publicCode());
  memberships.decide(t.agent(),memberships.list(t.agent()).getFirst().id(),true);
  memberships.remove(t.agent(),target,request);
  assertEquals(t.agent().id(),accounts.user(target).parentId());
  assertThrows(GameService.ApiError.class,()->memberships.remove(t.agent(),other.player().id(),request));
 }
 @Test void accountOverviewReturnsScopedClubBalances(){
  var t=tree();var other=tree();String target=t.player().id();
  db.update("UPDATE wallets SET balance=12345 WHERE player_id=?",target);
  db.update("UPDATE lobby_wallets SET balance=999999 WHERE player_id=?",target);
  var rows=accounts.overview(t.agent());
  assertTrue(rows.stream().noneMatch(a->a.id().equals(other.player().id())));
  assertEquals(12345,rows.stream().filter(a->a.id().equals(target)).findFirst().orElseThrow().clubChipsCents());
  assertTrue(accounts.overview(t.sa()).stream().anyMatch(a->a.id().equals(target)));
  assertTrue(accounts.overview(t.root()).stream().anyMatch(a->a.id().equals(other.player().id())));
  hierarchyChips.move(t.agent(),id(),target,"TAKE",100);
  assertEquals(12245,accounts.overview(t.agent()).stream().filter(a->a.id().equals(target)).findFirst().orElseThrow().clubChipsCents());
  assertEquals(999999,lobby.wallet(target,false).balanceCents());
 }
 @Test void agentCanPlayBothModesAndManagePlayers(){
  var t=tree();String agent=t.agent().id();
  db.update("UPDATE wallets SET balance=100000 WHERE player_id=?",agent);
  db.update("UPDATE lobby_wallets SET balance=100000 WHERE player_id=?",agent);
  String request=id();var club=game.spin(t.agent(),request,2000,0);
  var retry=game.spin(t.agent(),request,2000,0);
  assertEquals(club.revision(),retry.revision());assertEquals(club.balanceCents(),retry.balanceCents());
  assertEquals(100000,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id=?",Long.class,agent));
  assertEquals(agent,db.queryForObject("SELECT agent_id FROM round_ledger WHERE player_id=?",String.class,agent));
  assertEquals(t.sa().id(),db.queryForObject("SELECT super_agent_id FROM round_ledger WHERE player_id=?",String.class,agent));
  var gold=lobby.spin(t.agent(),id(),2000,0);
  assertEquals(club.balanceCents(),game.wallet(agent,false).balanceCents());
  String run=id();auto.start(t.agent(),run,10,2000,club.revision(),true);auto.process(agent);auto.stop(t.agent(),run);
  assertEquals(club.revision()+1,game.wallet(agent,false).revision());
  String lobbyRun=id();lobbyAuto.start(t.agent(),lobbyRun,10,2000,gold.revision(),true);lobbyAuto.process(agent);lobbyAuto.stop(t.agent(),lobbyRun);
  assertEquals(gold.revision()+1,lobby.wallet(agent,false).revision());
  long own=game.wallet(agent,false).balanceCents(),child=game.wallet(t.player().id(),false).balanceCents();
  hierarchyChips.move(t.agent(),id(),t.player().id(),"GIVE",100);
  assertEquals(own-100,game.wallet(agent,false).balanceCents());
  assertEquals(child+100,game.wallet(t.player().id(),false).balanceCents());
  hierarchyChips.move(t.agent(),id(),t.player().id(),"TAKE",100);
  assertEquals(own,game.wallet(agent,false).balanceCents());
  var outsider=tree();assertThrows(GameService.ApiError.class,()->hierarchyChips.move(t.agent(),id(),outsider.player().id(),"TAKE",100));
  assertTrue(Accounts.canPlay(t.sa()));
  assertTrue(Accounts.canPlay(t.root()));
 }
 @Test void agentOwnLossDoesNotEarnCommission(){
  var self=new Reports.Row("agent","Agent","agent","Agent",10000,0,-10000,1,1,0);
  var child=new Reports.Row("player","Player","agent","Agent",1000,200,-800,1,1,0);
  assertEquals(800,Reports.lossBase(List.of(self,child),"NET_AGENT"));
  assertEquals(800,Reports.lossBase(List.of(self,child),"POSITIVE_PLAYER"));
  assertEquals(0,Reports.lossBase(List.of(self),"POSITIVE_PLAYER"));
 }
 @Test void membershipApprovalMovesScopeWithoutMovingBalances(){
  var old=tree();var next=tree();var outsider=tree();
  long chipsBefore=game.wallet(old.player().id(),false).balanceCents();
  Long goldBefore=db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id=?",Long.class,old.player().id());
  memberships.request(old.player(),next.agent().publicCode());
  memberships.request(old.player(),next.agent().publicCode());
  assertEquals(1,memberships.list(next.agent()).size());
  String joinId=memberships.list(next.agent()).getFirst().id();
  assertTrue(memberships.list(outsider.agent()).isEmpty());
  assertThrows(GameService.ApiError.class,()->memberships.decide(outsider.agent(),joinId,true));
  assertEquals(old.agent().id(),accounts.user(old.player().id()).parentId());
  memberships.decide(next.agent(),joinId,true);
  memberships.decide(next.agent(),joinId,true);
  assertEquals(next.agent().id(),accounts.user(old.player().id()).parentId());
  assertFalse(accounts.inScope(old.agent(),accounts.user(old.player().id())));
  assertTrue(accounts.inScope(next.sa(),accounts.user(old.player().id())));
  assertEquals(chipsBefore,game.wallet(old.player().id(),false).balanceCents());
  assertEquals(goldBefore,db.queryForObject("SELECT balance FROM lobby_wallets WHERE player_id=?",Long.class,old.player().id()));
  assertThrows(GameService.ApiError.class,()->hierarchyChips.move(old.agent(),id(),old.player().id(),"TAKE",100));
  hierarchyChips.move(next.agent(),id(),old.player().id(),"TAKE",100);
  String transfer=id();hierarchyChips.move(next.agent(),transfer,old.player().id(),"GIVE",100);
  hierarchyChips.move(next.agent(),transfer,old.player().id(),"GIVE",100);
  assertEquals(chipsBefore,game.wallet(old.player().id(),false).balanceCents());
  assertThrows(GameService.ApiError.class,()->hierarchyChips.move(next.agent(),id(),old.player().id(),"GIVE",100));
 }
 @Test void membershipRejectAndRoleRestrictions(){
  var a=tree();var b=tree();
  assertThrows(GameService.ApiError.class,()->memberships.request(a.agent(),b.agent().publicCode()));
  assertThrows(GameService.ApiError.class,()->memberships.request(a.player(),b.sa().publicCode()));
  assertThrows(GameService.ApiError.class,()->memberships.request(a.player(),a.agent().publicCode()));
  memberships.request(a.player(),b.agent().publicCode());
  String joinId=memberships.list(b.agent()).getFirst().id();
  memberships.decide(b.agent(),joinId,false);
  assertEquals(a.agent().id(),accounts.user(a.player().id()).parentId());
  assertEquals("REJECTED",memberships.list(a.player()).getFirst().status());
  assertThrows(GameService.ApiError.class,()->memberships.decide(b.agent(),joinId,true));
  memberships.request(a.player(),b.agent().publicCode());
  assertNotEquals(joinId,memberships.list(b.agent()).getFirst().id());
  assertThrows(GameService.ApiError.class,()->memberships.decide(b.agent(),joinId,true));
  assertEquals(a.agent().id(),accounts.user(a.player().id()).parentId());
 }
 @Autowired GameService game;@Autowired Accounts accounts;@Autowired AutoService auto;@Autowired Reports reports;@Autowired Chips chips;@Autowired JdbcTemplate db;@Autowired MockMvc mvc;
 String id(){return UUID.randomUUID().toString();}
 Accounts.User creator(){return accounts.login("creator","test-only-creator-password").auth().user();}
 Accounts.User create(Accounts.User actor,Accounts.User parent,Accounts.Role role){String name="u"+id().replace("-","");return accounts.create(actor,name,name,"test-only-user-password",role,parent.id());}
 record Tree(Accounts.User root,Accounts.User sa,Accounts.User agent,Accounts.User player){}
 Tree tree(){var root=creator();var sa=create(root,root,Accounts.Role.SUPER_AGENT);var agent=create(root,sa,Accounts.Role.AGENT);var player=create(root,agent,Accounts.Role.PLAYER);db.update("UPDATE wallets SET balance=1000000 WHERE player_id=?",player.id());return new Tree(root,sa,agent,player);}
 @Test void roleHierarchyAndScope(){var a=tree();var b=tree();assertFalse(accounts.inScope(a.agent(),b.player()));assertTrue(accounts.inScope(a.sa(),a.player()));assertEquals("INVALID_PARENT",assertThrows(GameService.ApiError.class,()->create(a.agent(),b.agent(),Accounts.Role.PLAYER)).code);assertEquals("FORBIDDEN",assertThrows(GameService.ApiError.class,()->create(a.player(),a.agent(),Accounts.Role.PLAYER)).code);assertEquals("INVALID_PARENT",assertThrows(GameService.ApiError.class,()->create(a.root(),a.root(),Accounts.Role.PLAYER)).code);assertTrue(accounts.list(a.agent()).stream().allMatch(u->u.id().equals(a.agent().id())||u.id().equals(a.player().id())));}
 @Test void duplicateSpinIsExactlyOnceEvenConcurrent()throws Exception{var t=tree();String request=id();try(var pool=Executors.newFixedThreadPool(2)){Callable<GameService.SpinResult> task=()->game.spin(t.player(),request,2000,0);var a=pool.submit(task);var b=pool.submit(task);var ra=a.get(15,TimeUnit.SECONDS);var rb=b.get(15,TimeUnit.SECONDS);assertEquals(ra.balanceCents(),rb.balanceCents());assertEquals(ra.revision(),rb.revision());assertEquals(1,game.wallet(t.player().id(),false).revision());assertEquals(1000000-2000+ra.outcome().winCents(),ra.balanceCents());assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM round_ledger WHERE player_id=?",Integer.class,t.player().id()));}}
 @Test void staleStateAndReusedPayload(){var t=tree();String request=id();game.spin(t.player(),request,2000,0);assertEquals("STALE_STATE",assertThrows(GameService.ApiError.class,()->game.spin(t.player(),id(),2000,0)).code);assertEquals("REQUEST_REUSED",assertThrows(GameService.ApiError.class,()->game.spin(t.player(),request,5000,0)).code);}
 @Test void freeSpinLocksBetAndDoesNotDebit(){var t=tree();db.update("UPDATE wallets SET balance=0,free_spins=2,locked_bet=2000 WHERE player_id=?",t.player().id());assertEquals("BET_LOCKED",assertThrows(GameService.ApiError.class,()->game.spin(t.player(),id(),5000,0)).code);var r=game.spin(t.player(),id(),2000,0);assertTrue(r.freeSpin());assertEquals(r.outcome().winCents(),r.balanceCents());assertEquals(1+r.outcome().freeAward(),r.freeSpins());assertEquals(0,db.queryForObject("SELECT wager_cents FROM round_ledger WHERE player_id=?",Long.class,t.player().id()));}
 @Test void failedSpinLeavesWalletUnchanged(){var t=tree();db.update("UPDATE wallets SET balance=0 WHERE player_id=?",t.player().id());assertEquals("INSUFFICIENT_FUNDS",assertThrows(GameService.ApiError.class,()->game.spin(t.player(),id(),2000,0)).code);assertEquals(0,game.wallet(t.player().id(),false).revision());}
 @Test void autoCountersAreSeparateAndFinalBonusDrains(){var t=tree();String pid=t.player().id();db.update("UPDATE wallets SET free_spins=2,locked_bet=2000 WHERE player_id=?",pid);var job=auto.start(t.player(),id(),10,2000,0,true);auto.process(pid);assertEquals(0,auto.job(pid).completed());assertEquals(1,auto.job(pid).freeCompleted());assertEquals("AUTO_ACTIVE",assertThrows(GameService.ApiError.class,()->game.spin(t.player(),id(),2000,1)).code);
  db.update("UPDATE wallets SET free_spins=0,locked_bet=0 WHERE player_id=?",pid);db.update("UPDATE auto_jobs SET next_at=0 WHERE player_id=?",pid);auto.process(pid);assertEquals(1,auto.job(pid).completed());assertEquals(1,auto.job(pid).freeCompleted());
  db.update("UPDATE auto_jobs SET paid_done=10,next_at=0 WHERE player_id=?",pid);db.update("UPDATE wallets SET free_spins=1,locked_bet=2000 WHERE player_id=?",pid);auto.process(pid);assertEquals(10,auto.job(pid).completed());assertEquals(2,auto.job(pid).freeCompleted());assertEquals(game.wallet(pid,false).freeSpins()>0,auto.job(pid).active());auto.stop(t.player(),job.runId());assertFalse(auto.job(pid).active());long rev=game.wallet(pid,false).revision();auto.process(pid);assertEquals(rev,game.wallet(pid,false).revision());}
 @Test void concurrentAutoWorkersCannotDoubleSpin()throws Exception{var t=tree();auto.start(t.player(),id(),10,2000,0,true);try(var pool=Executors.newFixedThreadPool(4)){var jobs=new ArrayList<Future<?>>();for(int i=0;i<4;i++)jobs.add(pool.submit(()->auto.process(t.player().id())));for(var f:jobs)f.get(15,TimeUnit.SECONDS);}assertEquals(1,auto.job(t.player().id()).completed());assertEquals(1,game.wallet(t.player().id(),false).revision());}
 @Test void transfersReserveRefundAndApproveExactlyOnce()throws Exception{var t=tree();String request=id();var w=chips.request(t.player(),request,"WITHDRAWAL",12345,"test");assertEquals(987655,game.wallet(t.player().id(),false).balanceCents());assertEquals(w.id(),chips.request(t.player(),request,"WITHDRAWAL",12345,"test").id());assertEquals(987655,game.wallet(t.player().id(),false).balanceCents());chips.decide(t.agent(),w.id(),false);chips.decide(t.agent(),w.id(),false);assertEquals(1000000,game.wallet(t.player().id(),false).balanceCents());db.update("UPDATE wallets SET balance=100000 WHERE player_id=?",t.agent().id());var d=chips.request(t.player(),id(),"DEPOSIT",54321,"test");assertEquals(1000000,game.wallet(t.player().id(),false).balanceCents());try(var pool=Executors.newFixedThreadPool(2)){var a=pool.submit(()->chips.decide(t.agent(),d.id(),true));var b=pool.submit(()->chips.decide(t.agent(),d.id(),true));a.get(15,TimeUnit.SECONDS);b.get(15,TimeUnit.SECONDS);}assertEquals(1054321,game.wallet(t.player().id(),false).balanceCents());assertEquals(0,reports.report(t.player(),"week",reports.today()).netCents());var outsider=tree();assertEquals("FORBIDDEN",assertThrows(GameService.ApiError.class,()->chips.decide(outsider.agent(),d.id(),true)).code);}
 void ledger(Tree t,Accounts.User p,long time,long bet,long payout,boolean free,long rev){db.update("INSERT INTO round_ledger(player_id,request_id,agent_id,super_agent_id,nominal_bet,wager_cents,payout_cents,is_free,wallet_revision,response_json,created_at) VALUES(?,?,?,?,?,?,?,?,?,'{}',?)",p.id(),id(),t.agent().id(),t.sa().id(),2000,bet,payout,free,rev,time);}
 @Test void weeklyTimezoneBoundariesFreePayoutsAndIsolation(){var t=tree();var p=reports.period("week",LocalDate.of(2026,8,5));assertEquals("2026-08-03",p.start());assertEquals(Instant.parse("2026-08-02T16:00:00Z").toEpochMilli(),p.from());ledger(t,t.player(),p.from()-1,2000,0,false,1);ledger(t,t.player(),p.from(),2000,100,false,2);ledger(t,t.player(),p.to()-1,0,500,true,3);ledger(t,t.player(),p.to(),2000,10000,false,4);var result=reports.report(t.agent(),"week",LocalDate.of(2026,8,5));assertEquals(2000,result.wagerCents());assertEquals(600,result.payoutCents());assertEquals(-1400,result.netCents());assertEquals(1,result.players().getFirst().freeSpins());assertEquals(0,reports.report(tree().agent(),"week",LocalDate.of(2026,8,5)).players().size());var ranks=(List<Reports.Rank>)reports.ranking("week",LocalDate.of(2026,8,5)).get("players");assertEquals(2000,ranks.stream().filter(r->r.displayName().equals(t.player().displayName())).findFirst().orElseThrow().wagerCents());}
 @Test void commissionModesAndSnapshotApproval(){var t=tree();var winner=create(t.root(),t.agent(),Accounts.Role.PLAYER);LocalDate date=LocalDate.of(2026,7,6);var p=reports.period("week",date);db.update("UPDATE accounts SET created_at=? WHERE id=?",p.from()-1,t.agent().id());ledger(t,t.player(),p.from(),100000,20000,false,1);ledger(t,winner,p.from(),100000,130000,false,1);var settings=reports.settings();reports.settings(t.root(),3000,"POSITIVE_PLAYER",settings.revision());reports.closeWeek(date);var s=reports.settlements(t.agent(),date).getFirst();assertEquals(80000,s.lossBaseCents());assertEquals(24000,s.commissionCents());assertEquals(50000,Reports.lossBase(s.players(),"NET_AGENT"));assertEquals(15000,Reports.commission(50000,3000));
  reports.decide(t.root(),t.agent().id(),date,"APPROVE",2500,s.revision(),"reviewed");var approved=reports.settlements(t.agent(),date).getFirst();assertEquals(20000,approved.commissionCents());reports.decide(t.root(),t.agent().id(),date,"PAID",2500,approved.revision(),"record only");assertEquals("PAID",reports.settlements(t.agent(),date).getFirst().status());reports.closeWeek(date);assertEquals(1,reports.settlements(t.agent(),date).size());assertEquals(1000000,game.wallet(t.player().id(),false).balanceCents());assertThrows(GameService.ApiError.class,()->reports.decide(t.agent(),t.agent().id(),date,"PAID",2500,2,""));}
 @Test void loginLockoutAndPasswordRevokesSessions(){var t=tree();for(int i=0;i<5;i++)assertEquals("BAD_LOGIN",assertThrows(GameService.ApiError.class,()->accounts.login(t.player().username(),"incorrect")).code);assertEquals("LOGIN_LOCKED",assertThrows(GameService.ApiError.class,()->accounts.login(t.player().username(),"test-only-user-password")).code);db.update("UPDATE accounts SET locked_until=0 WHERE id=?",t.player().id());var login=accounts.login(t.player().username(),"test-only-user-password");accounts.password(login.auth(),"test-only-user-password","another-test-password");assertThrows(GameService.ApiError.class,()->accounts.authenticate(login.token()));assertNotNull(accounts.login(t.player().username(),"another-test-password"));}
 @Test void apiRejectsOriginCsrfAndForgedFields()throws Exception{mvc.perform(post("/api/login")).andExpect(status().isForbidden());mvc.perform(post("/api/login").header("X-Game-Client","web").header("Origin","https://attacker.invalid")).andExpect(status().isForbidden());var t=tree();var login=accounts.login(t.player().username(),"test-only-user-password");var c=new Cookie("ACE_SESSION",login.token());mvc.perform(post("/api/spins").cookie(c).header("X-Game-Client","web").contentType(MediaType.APPLICATION_JSON).content("{\"requestId\":\""+id()+"\",\"betCents\":2000,\"expectedRevision\":0}")).andExpect(status().isForbidden());mvc.perform(post("/api/spins").cookie(c).header("X-Game-Client","web").header("X-CSRF-Token",login.auth().csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"requestId\":\""+id()+"\",\"betCents\":2000,\"expectedRevision\":0,\"balance\":999999}")).andExpect(status().isBadRequest());mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());mvc.perform(get("/")).andExpect(status().isOk());}
}
