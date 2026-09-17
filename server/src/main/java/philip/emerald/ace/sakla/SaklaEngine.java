package philip.emerald.ace.sakla;
import philip.emerald.ace.Utils.ArcadeEngine.Outcome;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** 40-card Spanish deck. Stop at the first completed fixed pair. */
public final class SaklaEngine {
 public static final List<Integer> RANKS=List.of(1,2,3,4,5,6,7,10,11,12);
 private final IntUnaryOperator random;
 public SaklaEngine(){this(new SecureRandom()::nextInt);}
 public SaklaEngine(IntUnaryOperator random){this.random=random;}
 public static int pair(int card){if(card<0||card>=40)throw new IllegalArgumentException("card");int rank=card%10;return card/10*5+(rank==0||rank==9?0:rank<=2?1:rank<=4?2:rank<=6?3:4);}
 public Outcome play(int selection){if(selection<0||selection>=20)throw new IllegalArgumentException("pair");var deck=new ArrayList<Integer>();for(int i=0;i<40;i++)deck.add(i);for(int i=39;i>0;i--)Collections.swap(deck,i,random.applyAsInt(i+1));int[] counts=new int[20];var draws=new ArrayList<Integer>();for(int card:deck){draws.add(card);int pair=pair(card);if(++counts[pair]==2)return new Outcome(draws,pair,pair==selection?1900:0,"SAKLA");}throw new IllegalStateException("no pair");}
}
