package philip.emerald.ace.Utils;
import philip.emerald.ace.superace.LobbyAutoService;
import philip.emerald.ace.AceApplication;
import philip.emerald.ace.superace.LobbyGameService;
import philip.emerald.ace.superace.GameService;
import org.springframework.boot.SpringApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.flywaydb.core.Flyway;
import java.util.*;
import java.util.concurrent.*;

/** Disposable database checks; does not access deployment data. */
public class ModeIntegration {
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 static String id(){return UUID.randomUUID().toString();}
 public static void main(String[] args)throws Exception{
  String url="jdbc:h2:mem:modes;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
  boolean upgrade=args.length>0&&args[0].equals("upgrade");
  boolean sql=args.length>0&&args[0].equals("sql");
  if(sql){String script=java.nio.file.Files.readString(java.nio.file.Path.of("database/super_ace.sql"));script=script.replaceAll("(?m)^CREATE DATABASE[^\\n]*\\n|^USE ace;\\n","").replace("ROUND(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3))*1000)",Long.toString(System.currentTimeMillis()));try(var c=java.sql.DriverManager.getConnection(url,"sa","")){org.h2.tools.RunScript.execute(c,new java.io.StringReader(script));}}
  if(upgrade){
   Flyway.configure().dataSource(url,"sa","").target("4").load().migrate();
   try(var c=java.sql.DriverManager.getConnection(url,"sa","");var s=c.createStatement()){
    s.execute("INSERT INTO accounts(id,username,display_name,password_hash,role,created_at,public_code) VALUES('oldcreator','creator','Existing owner','unused','CREATOR',1,'123123')");
    s.execute("INSERT INTO wallets(player_id,balance) VALUES('oldcreator',7654321)");
   }
  }
  var app=new SpringApplication(AceApplication.class);
  try(var ctx=app.run("--server.port=0","--spring.datasource.url="+url,"--spring.datasource.username=sa","--spring.datasource.password=","--ace.creator-password=test-only-creator-password","--ace.jwt-secret=test-only-secret-at-least-32-characters","--ace.jobs-enabled=false","--ace.spin-cooldown-ms=0")){
   var db=ctx.getBean(JdbcTemplate.class);var accounts=ctx.getBean(Accounts.class);var registration=ctx.getBean(DirectRegistration.class);
   var club=ctx.getBean("gameService",GameService.class);var lobby=ctx.getBean(LobbyGameService.class);var auto=ctx.getBean(LobbyAutoService.class);
   check(club.mode().equals("CLUB"),"primary game must be CLUB");
   check(db.queryForObject("SELECT COUNT(*) FROM clubs WHERE public_code='686868' AND name='LUCKY SEVEN' AND owner_id IS NOT NULL",Integer.class)==1,"club initialized with owner");
   var owner=accounts.user(db.queryForObject("SELECT id FROM accounts WHERE role='CREATOR' ORDER BY created_at LIMIT 1",String.class));
   if(upgrade)check(club.wallet(owner.id(),false).balanceCents()==7654321,"existing balance preserved");
   if(sql)check(accounts.login("zuchiha","112357").auth().user().role()==Accounts.Role.CREATOR,"SQL seeded Creator can login");
   var login=registration.register("p"+id().replace("-",""),"123456");var p=login.auth().user();
   check(lobby.wallet(p.id(),false).balanceCents()==1000000&&club.wallet(p.id(),false).balanceCents()==0,"initial split");
   check(db.queryForObject("SELECT club_id FROM accounts WHERE id=?",String.class,p.id()).endsWith("686868"),"club membership");
   check(db.queryForObject("SELECT COUNT(*) FROM chip_transfers WHERE player_id=?",Integer.class,p.id())==0,"no welcome Club credit");
   String request=id();try(var pool=Executors.newFixedThreadPool(2)){
    var a=pool.submit(()->lobby.spin(p,request,5000,0));var b=pool.submit(()->lobby.spin(p,request,5000,0));
    var first=a.get(20,TimeUnit.SECONDS);var second=b.get(20,TimeUnit.SECONDS);check(first.requestId().equals(second.requestId())&&first.balanceCents()==second.balanceCents(),"identical retry response");
   }
   check(lobby.wallet(p.id(),false).revision()==1&&club.wallet(p.id(),false).revision()==0,"concurrent retry is once and isolated");
   check(db.queryForObject("SELECT rtp_profile FROM lobby_round_ledger WHERE player_id=?",String.class,p.id()).equals("LOBBY_98"),"lobby RTP");
   db.update("UPDATE lobby_wallets SET free_spins=2,locked_bet=5000,bonus_profile='LOBBY_98' WHERE player_id=?",p.id());
   auto.start(p,id(),10,5000,1,true);auto.process(p.id());
   check(auto.job(p.id()).completed()==0&&auto.job(p.id()).freeCompleted()==1,"free does not reduce paid count");
   check(club.wallet(p.id(),false).freeSpins()==0,"free spins isolated");
   try{accounts.change(owner,p.id(),Accounts.Role.AGENT,accounts.user(p.parentId()).parentId(),true);throw new AssertionError("promotion during Lobby bonus allowed");}catch(GameService.ApiError e){check(e.code.equals("FINISH_GAME_FIRST"),"promotion guard");}
   accounts.change(owner,p.id(),Accounts.Role.PLAYER,p.parentId(),false);check(!auto.job(p.id()).active(),"disable stops Lobby auto");
   check(ctx.getBean(Reports.class).report(owner,"week",ctx.getBean(Reports.class).today()).wagerCents()==0,"Gold excluded from commission reports");
   long count=db.queryForObject("SELECT COUNT(*) FROM accounts",Long.class);registration.initialize();check(db.queryForObject("SELECT COUNT(*) FROM accounts",Long.class)==count,"idempotent club initialization");
   System.out.println("PASS "+(sql?"SQL import and Flyway checksum validation":upgrade?"V4 upgrade":"fresh database")+": club owner, dual balances, concurrent retry, RTP, free/paid counters, promotion and disable guards, commission isolation, idempotent initialization.");
  }
 }
}
