package vn.emerald.ace;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;

/** Pure server-side mathematics. Amounts are integer hundredths of a virtual coin. */
public class GameEngine {
 public static final long MIN_BET=500L,MAX_BET=50000L;
 public static boolean validBet(long bet){return bet>=MIN_BET&&bet<=MAX_BET;}
 private static final String[] SYMBOLS={"A","K","Q","J","10","9","8","7","W","S"};
 private static final int[] WEIGHTS={8,10,12,14,16,18,20,22,1,3};
 private static final int[] PAY={3400,2125,2125,1275,1275,850,680,510};
 private static final int[] MULTIPLIERS={1,2,3,5};
 private final IntUnaryOperator random;
 private double payoutScale=1;
 public GameEngine(IntUnaryOperator random,String profile){this(random);payoutScale=scaleFor(profile);}
 public static double scaleFor(String profile){if(profile.matches("RTP_[0-9]{3,5}")){int bps=Integer.parseInt(profile.substring(4));if(bps<100||bps>10000)throw new IllegalArgumentException("Invalid RTP");return (bps/10000.0)/0.99315698487;}return switch(profile){case "CLUB_97","INTRO_97"->0.97/0.99315698487;case "LOBBY_98"->0.98/0.99315698487;case "STANDARD_96"->0.96/0.99315698487;case "LEGACY"->1;default->throw new IllegalArgumentException("Unknown RTP profile");};}
 public GameEngine(String profile){this(new SecureRandom()::nextInt,profile);}
 public GameEngine(){SecureRandom secure=new SecureRandom();random=secure::nextInt;}
 public GameEngine(IntUnaryOperator random){this.random=random;}
 public record Evaluation(long units,Set<String> hits) {}
 public record Cascade(List<String> hits,int multiplier,long winCents,String[][] nextBoard) {}
 public record Outcome(String[][] initialBoard,List<Cascade> cascades,String[][] finalBoard,int freeAward,long winCents) {}
 public static String[][] copy(String[][] grid){return Arrays.stream(grid).map(String[]::clone).toArray(String[][]::new);}
 public String symbol(){int value=random.applyAsInt(124);for(int i=0;i<WEIGHTS.length;i++){value-=WEIGHTS[i];if(value<0)return SYMBOLS[i];}throw new IllegalStateException("Invalid random value");}
 public String[][] board(){String[][] g=new String[5][4];for(int c=0;c<5;c++)for(int r=0;r<4;r++)g[c][r]=symbol();return g;}
 public Evaluation evaluate(String[][] grid){
  Set<String> hits=new LinkedHashSet<>();long units=0;
  for(int si=0;si<8;si++){String symbol=SYMBOLS[si];int ways=1,wildWays=1,cols=0,realCols=0;List<String> positions=new ArrayList<>();
   for(int c=0;c<5;c++){int count=0,wilds=0;boolean real=false;for(int r=0;r<4;r++){String s=grid[c][r];if(s.equals(symbol)||s.equals("W")){count++;positions.add(c+","+r);}if(s.equals("W"))wilds++;if(s.equals(symbol))real=true;}if(count==0)break;cols++;ways*=count;wildWays*=wilds;if(real)realCols++;}
   if(cols>=3&&ways>wildWays){units+=(long)PAY[si]*(1L<<(cols-3))*(ways-wildWays);for(String pos:positions){int c=pos.charAt(0)-'0',r=pos.charAt(2)-'0';boolean columnHas=Arrays.asList(grid[c]).contains(symbol);if(grid[c][r].equals(symbol)||realCols-(columnHas?1:0)>0)hits.add(pos);}}
  }return new Evaluation(units,hits);
 }
 public Outcome spin(long bet){return spin(bet,board());}
 public Outcome spin(long bet,String[][] first){
  if(!validBet(bet))throw new IllegalArgumentException("Invalid bet");
  String[][] grid=copy(first),initial=copy(first);int scatter=0;for(String[] col:grid)for(String s:col)if(s.equals("S"))scatter++;
  int award=scatter>=3?10:0;long total=0;List<Cascade> steps=new ArrayList<>();
  for(int count=0;count<1000;count++){
   Evaluation e=evaluate(grid);if(e.hits().isEmpty())return new Outcome(initial,List.copyOf(steps),copy(grid),award,total);
   int multiplier=MULTIPLIERS[Math.min(count,3)];long raw=Math.multiplyExact(Math.multiplyExact(bet,e.units()),multiplier);long win=Math.round(raw*payoutScale/10000.0);total=Math.addExact(total,win);
   for(int c=0;c<5;c++){List<String> keep=new ArrayList<>();for(int r=0;r<4;r++)if(!e.hits().contains(c+","+r))keep.add(grid[c][r]);int missing=4-keep.size();for(int r=0;r<missing;r++)grid[c][r]=symbol();for(int r=0;r<keep.size();r++)grid[c][missing+r]=keep.get(r);}
   steps.add(new Cascade(List.copyOf(e.hits()),multiplier,win,copy(grid)));
  }throw new IllegalStateException("Cascade guard reached; transaction must roll back");
 }
}
