package philip.emerald.ace.Utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ArcadeEngineTest {
 @Test void everyPlinkoRouteLandsInItsPaidBin(){long sum=0;for(int path=0;path<4096;path++){final int bits=path;int[] index={0};var result=new ArcadeEngine(bound->(bits>>index[0]++)&1).play(ArcadeEngine.Game.PLINKO);assertEquals(12,result.values().size());assertEquals(Integer.bitCount(path),result.slot());assertEquals(ArcadeEngine.PLINKO.get(result.slot()).intValue(),result.multiplierBps());sum+=ArcadeEngine.payout(10000,result);}assertEquals(39568000,sum);}
 @Test void allWheelSectorsAreEquallySelectable(){long sum=0;for(int i=0;i<16;i++){final int slot=i;var r=new ArcadeEngine(bound->slot).play(ArcadeEngine.Game.WHEEL);assertEquals(i,r.slot());assertEquals(ArcadeEngine.WHEEL.get(i).intValue(),r.multiplierBps());sum+=ArcadeEngine.payout(10000,r);}assertEquals(155000,sum);}
 @Test void exhaustiveSlotsPaytable(){long total=0;for(int n=0;n<1000;n++){final int value=n;int[] index={0};var r=new ArcadeEngine(bound->{int d=index[0]++;return value/(int)Math.pow(10,d)%10;}).play(ArcadeEngine.Game.SLOTS);assertEquals(3,r.values().size());int a=r.values().getFirst();boolean triple=r.values().stream().allMatch(v->v==a);long sevens=r.values().stream().filter(v->v==3).count();int expected=triple?java.util.List.of(500,1000,2500,10000).get(a):sevens==2?300:0;assertEquals(expected,r.multiplierBps());total+=ArcadeEngine.payout(10000,r);}assertEquals(9710000,total);}
 @Test void fractionalPayoutRoundsDown(){var r=new ArcadeEngine(bound->0).play(ArcadeEngine.Game.PLINKO);assertEquals(25000,ArcadeEngine.payout(500,r));var center=new ArcadeEngine.Outcome(java.util.List.of(),6,50,"PLINKO");assertEquals(250,ArcadeEngine.payout(501,center));}
}
