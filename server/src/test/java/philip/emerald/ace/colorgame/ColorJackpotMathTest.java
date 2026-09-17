package philip.emerald.ace.colorgame;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ColorJackpotMathTest {
 @Test void exactWeightsAndBounds(){var counts=new EnumMap<ColorJackpotMath.Tier,Integer>(ColorJackpotMath.Tier.class);for(int i=0;i<10000;i++)counts.merge(ColorJackpotMath.tier(i),1,Integer::sum);for(var t:ColorJackpotMath.Tier.values())assertEquals(t.weight,counts.get(t));assertThrows(IllegalArgumentException.class,()->ColorJackpotMath.tier(10000));assertThrows(IllegalArgumentException.class,()->ColorJackpotMath.tier(-1));}
 @Test void prizesAndSplitsConserveSmallAndHugeAmounts(){for(var t:ColorJackpotMath.Tier.values())assertEquals(t.prizeBps*100L,ColorJackpotMath.prize(1000000,t));assertEquals(Map.of("a",1L,"b",1L,"c",0L),ColorJackpotMath.split(2,Map.of("a",1L,"b",1L,"c",1L)));assertEquals(Map.of("a",1000L,"b",4000L),ColorJackpotMath.split(5000,Map.of("a",100L,"b",400L)));var parts=ColorJackpotMath.split(Long.MAX_VALUE,Map.of("a",Long.MAX_VALUE,"b",Long.MAX_VALUE));assertEquals(Long.MAX_VALUE,Math.addExact(parts.get("a"),parts.get("b")));}
 @Test void splittingClicksDoesNotLoseFractionalContribution(){var all=ColorJackpotMath.contribution(301,0);long credited=0;int remainder=0;for(long n:new long[]{1,99,101,100}){var c=ColorJackpotMath.contribution(n,remainder);credited+=c.cents();remainder=c.remainder();}assertEquals(all.cents(),credited);assertEquals(all.remainder(),remainder);assertEquals(0,ColorJackpotMath.prize(1,ColorJackpotMath.Tier.MINI));}
}
