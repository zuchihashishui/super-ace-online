package philip.emerald.ace.luckynine;
import philip.emerald.ace.Utils.Decks;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** Explicit house variant: naturals 8/9; banker draws 0-5, stands 6-9. */
public final class LuckyNineEngine {
 public enum Action { HIT, STAND }
 public record Outcome(List<Integer> playerCards,List<Integer> bankerCards,int playerTotal,int bankerTotal,String status,int multiplierBps){public Outcome{playerCards=List.copyOf(playerCards);bankerCards=List.copyOf(bankerCards);}}
 private final IntUnaryOperator random;
 public LuckyNineEngine(){this(new SecureRandom()::nextInt);}
 public LuckyNineEngine(IntUnaryOperator random){this.random=random;}
 public List<Integer> deck(){return Decks.shuffle(52,random);}
 public static int total(List<Integer> cards){return cards.stream().mapToInt(c->Math.min(c%13+1,10)%10).sum()%10;}
 public static List<Integer> player(List<Integer> deck){return List.of(deck.get(0),deck.get(2));}
 public static boolean natural(List<Integer> deck){return total(player(deck))>=8||total(List.of(deck.get(1),deck.get(3)))>=8;}
 public static Outcome finish(List<Integer> deck,Action action){var p=new ArrayList<>(player(deck));var b=new ArrayList<>(List.of(deck.get(1),deck.get(3)));int next=4;
  if(!natural(deck)){if(action==null)throw new IllegalArgumentException("decision required");if(action==Action.HIT)p.add(deck.get(next++));if(total(b)<=5)b.add(deck.get(next));}
  int a=total(p),bank=total(b);String status=a>bank?"WIN":a==bank?"TIE":"LOST";return new Outcome(p,b,a,bank,status,a>bank?195:a==bank?100:0);
 }
 public static long payout(long stake,Outcome r){return Math.multiplyExact(stake,r.multiplierBps())/100;}
}
