package philip.emerald.ace.mines;

import java.security.SecureRandom;
import java.util.*;
public final class MinesEngine {
 public static final List<Integer> COUNTS=List.of(1,3,5,10);
 private final SecureRandom random=new SecureRandom();
 public List<Integer> board(int mines){if(!COUNTS.contains(mines))throw new IllegalArgumentException();var tiles=new ArrayList<Integer>();for(int i=0;i<25;i++)tiles.add(i);for(int i=24;i>0;i--){int j=random.nextInt(i+1);Collections.swap(tiles,i,j);}return List.copyOf(tiles.subList(0,mines));}
 static long choose(int n,int k){long result=1;for(int i=1;i<=k;i++)result=result*(n-i+1)/i;return result;}
 public static long multiplier(int mines,int opened){if(!COUNTS.contains(mines)||opened<0||opened>25-mines)throw new IllegalArgumentException();return opened==0?100:97*choose(25,opened)/choose(25-mines,opened);}
 public static long payout(long stake,int mines,int opened){return Math.multiplyExact(stake,multiplier(mines,opened))/100;}
}
