package vn.emerald.ace;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ColorGameEngineTest {
 @Test void all216OutcomesPayByNumberOfMatches(){
  long total=0;for(var a:ColorGameEngine.Side.values())for(var b:ColorGameEngine.Side.values())for(var c:ColorGameEngine.Side.values()){
   var o=new ColorGameEngine.Outcome(List.of(a,b,c));for(var side:ColorGameEngine.Side.values()){long count=o.dice().stream().filter(v->v==side).count();assertEquals(count==0?0:500*(count+1),ColorGameEngine.payout(500,side,o));}total+=ColorGameEngine.payout(500,ColorGameEngine.Side.RED,o);
  }assertEquals(199*500,total);
 }
 @Test void threeDiceCanRepeatAndUseAllSixColors(){
  var random=new Random(617);var engine=new ColorGameEngine(random::nextInt);var seen=EnumSet.noneOf(ColorGameEngine.Side.class);boolean triple=false;
  for(int i=0;i<10000;i++){var o=engine.deal();assertEquals(3,o.dice().size());seen.addAll(o.dice());if(new HashSet<>(o.dice()).size()==1)triple=true;}assertEquals(6,seen.size());assertTrue(triple);
 }
 @Test void rejectsInvalidStakeAndHand(){assertThrows(IllegalArgumentException.class,()->new ColorGameEngine.Outcome(List.of()));assertThrows(IllegalArgumentException.class,()->ColorGameEngine.payout(499,ColorGameEngine.Side.RED,new ColorGameEngine().deal()));}
}
