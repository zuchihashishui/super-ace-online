package philip.emerald.ace.colorgame;

import java.math.BigInteger;
import java.util.*;

/** Version 1 public rules. Only money allocation; no manipulation of dice results. */
public final class ColorJackpotMath {
 private ColorJackpotMath(){}
 public enum Tier {
  GRAND(5000,10), MAJOR(2000,90), MINOR(500,900), MINI(100,9000);
  public final int prizeBps,weight;
  Tier(int prizeBps,int weight){this.prizeBps=prizeBps;this.weight=weight;}
 }
 public static Tier tier(int draw){if(draw<0||draw>=10000)throw new IllegalArgumentException("draw");for(var t:Tier.values()){if(draw<t.weight)return t;draw-=t.weight;}throw new IllegalStateException();}
 public static long prize(long pool,Tier tier){if(pool<0)throw new IllegalArgumentException("pool");return BigInteger.valueOf(pool).multiply(BigInteger.valueOf(tier.prizeBps)).divide(BigInteger.valueOf(10000)).longValueExact();}
 public record Contribution(long cents,int remainder){}
 public static Contribution contribution(long stake,int remainder){if(stake<0||remainder<0||remainder>=100)throw new IllegalArgumentException();var parts=BigInteger.valueOf(stake).add(BigInteger.valueOf(remainder)).divideAndRemainder(BigInteger.valueOf(100));return new Contribution(parts[0].longValueExact(),parts[1].intValueExact());}
 public static SortedMap<String,Long> split(long prize,Map<String,Long> stakes){
  if(prize<0||stakes.isEmpty()||stakes.values().stream().anyMatch(n->n==null||n<=0))throw new IllegalArgumentException();
  BigInteger total=stakes.values().stream().map(BigInteger::valueOf).reduce(BigInteger.ZERO,BigInteger::add),amount=BigInteger.valueOf(prize);
  var result=new TreeMap<String,Long>();var remainders=new HashMap<String,BigInteger>();long assigned=0;
  for(var e:stakes.entrySet()){var parts=amount.multiply(BigInteger.valueOf(e.getValue())).divideAndRemainder(total);long n=parts[0].longValueExact();result.put(e.getKey(),n);remainders.put(e.getKey(),parts[1]);assigned=Math.addExact(assigned,n);}
  var order=new ArrayList<>(result.keySet());order.sort(Comparator.<String,BigInteger>comparing(remainders::get).reversed().thenComparing(Comparator.naturalOrder()));
  for(int i=0;i<prize-assigned;i++)result.compute(order.get(i),(id,n)->n+1);
  return result;
 }
}
