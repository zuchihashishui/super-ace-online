package philip.emerald.ace.luckynine;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class LuckyNineEngineTest {
 @Test void ranksAndModulo(){for(int c=0;c<52;c++)assertEquals(Math.min(c%13+1,10)%10,LuckyNineEngine.total(List.of(c)));assertEquals(5,LuckyNineEngine.total(List.of(6,7)));}
 @Test void shuffledDeckIsComplete(){for(int s=0;s<1000;s++){var deck=new LuckyNineEngine(new Random(s)::nextInt).deck();assertEquals(52,deck.size());assertEquals(52,new HashSet<>(deck).size());assertEquals(0,Collections.min(deck));assertEquals(51,Collections.max(deck));}}
 @Test void hitStandAndBankerThreshold(){var deck=List.of(0,1,2,3,4,5);assertFalse(LuckyNineEngine.natural(deck));var hit=LuckyNineEngine.finish(deck,LuckyNineEngine.Action.HIT);assertEquals(List.of(0,2,4),hit.playerCards());assertEquals(List.of(1,3),hit.bankerCards());assertEquals("WIN",hit.status());assertEquals(975,LuckyNineEngine.payout(500,hit));var stand=LuckyNineEngine.finish(deck,LuckyNineEngine.Action.STAND);assertEquals("LOST",stand.status());assertEquals(0,LuckyNineEngine.payout(500,stand));var drawn=LuckyNineEngine.finish(List.of(2,0,3,1,13,14),LuckyNineEngine.Action.STAND);assertEquals(List.of(0,1,13),drawn.bankerCards());assertEquals("WIN",drawn.status());}
 @Test void naturalEightNineStopBothHands(){for(var action:LuckyNineEngine.Action.values()){var win=LuckyNineEngine.finish(List.of(3,0,4,1,2,5),action);assertEquals(2,win.playerCards().size());assertEquals(2,win.bankerCards().size());assertEquals("WIN",win.status());var loss=LuckyNineEngine.finish(List.of(0,3,1,4,2,5),action);assertEquals("LOST",loss.status());var tie=LuckyNineEngine.finish(List.of(3,16,4,17,0,1),action);assertEquals("TIE",tie.status());assertEquals(500,LuckyNineEngine.payout(500,tie));}}
 @Test void tieRefundsAndProfitRoundsDown(){var tie=LuckyNineEngine.finish(List.of(0,13,2,15,9,10),LuckyNineEngine.Action.STAND);assertEquals("TIE",tie.status());assertEquals(501,LuckyNineEngine.payout(501,tie));var win=LuckyNineEngine.finish(List.of(0,1,2,3,4,5),LuckyNineEngine.Action.HIT);assertEquals(976,LuckyNineEngine.payout(501,win));}
}
