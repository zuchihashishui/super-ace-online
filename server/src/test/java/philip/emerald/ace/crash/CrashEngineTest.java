package philip.emerald.ace.crash;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CrashEngineTest {
 @Test void allMillionDrawsAreBoundedAndDistributionMatchesRules(){int above2=0,above10=0;for(int d=1;d<=1000000;d++){int p=CrashEngine.fromDraw(d);assertTrue(p>=100&&p<=10000);if(p>200)above2++;if(p>1000)above10++;}assertEquals(97000000/201,above2);assertEquals(97000000/1001,above10);assertEquals(100,CrashEngine.fromDraw(1000000));assertEquals(10000,CrashEngine.fromDraw(1));}
 @Test void payoutRoundingAndTimeBoundaries(){assertEquals(668,CrashEngine.payout(503,133));assertEquals(100,CrashEngine.multiplier(1000,999));assertEquals(1000,CrashEngine.at(1000,100));long at=CrashEngine.at(1000,200);assertTrue(CrashEngine.multiplier(1000,at)>=200);assertTrue(CrashEngine.multiplier(1000,at-1)<200);assertTrue(CrashEngine.validAuto(0));assertTrue(CrashEngine.validAuto(101));assertFalse(CrashEngine.validAuto(100));assertFalse(CrashEngine.validAuto(10000));}
}
