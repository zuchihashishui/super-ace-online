package philip.emerald.ace.bingo;
import philip.emerald.ace.Utils.Decks;
import philip.emerald.ace.Utils.ArcadeEngine.Outcome;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** A personal 75-ball ticket: exactly 30 calls, at least one straight line wins once. */
public final class BingoEngine {
 public static final int CALLS=30,RETURN=675;
 private final IntUnaryOperator random;
 public BingoEngine(){this(new SecureRandom()::nextInt);}
 public BingoEngine(IntUnaryOperator random){this.random=random;}
 public static List<List<Integer>> lines(){var rows=new ArrayList<List<Integer>>();for(int r=0;r<5;r++){var line=new ArrayList<Integer>();for(int c=0;c<5;c++)line.add(r*5+c);rows.add(List.copyOf(line));}for(int c=0;c<5;c++){var line=new ArrayList<Integer>();for(int r=0;r<5;r++)line.add(r*5+c);rows.add(List.copyOf(line));}rows.add(List.of(0,6,12,18,24));rows.add(List.of(4,8,12,16,20));return List.copyOf(rows);}
 public static int winningLines(List<Integer> card,List<Integer> calls){Set<Integer> marked=new HashSet<>(calls);marked.add(0);int mask=0;var lines=lines();for(int i=0;i<lines.size();i++){boolean complete=true;for(int cell:lines.get(i))complete&=marked.contains(card.get(cell));if(complete)mask|=1<<i;}return mask;}
 public Outcome play(){var card=new ArrayList<>(Collections.nCopies(25,0));for(int col=0;col<5;col++){var nums=new ArrayList<>(Decks.shuffle(15,random).subList(0,col==2?4:5));Collections.sort(nums);int pos=0;for(int row=0;row<5;row++)if(row!=2||col!=2)card.set(row*5+col,col*15+nums.get(pos++)+1);}
  var calls=Decks.shuffle(75,random).subList(0,CALLS).stream().map(n->n+1).toList();int wins=winningLines(card,calls);var values=new ArrayList<>(card);values.addAll(calls);return new Outcome(values,wins,wins==0?0:RETURN,"BINGO");
 }
}
