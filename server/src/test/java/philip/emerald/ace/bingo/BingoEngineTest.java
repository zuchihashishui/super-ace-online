package philip.emerald.ace.bingo;
import philip.emerald.ace.Utils.ArcadeEngine;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class BingoEngineTest {
 @Test void independentTicketsHaveLegalColumnsAndUniqueCalls(){for(int s=0;s<3000;s++){var r=new BingoEngine(new Random(s)::nextInt).play();var card=r.values().subList(0,25);var calls=r.values().subList(25,55);assertEquals(0,card.get(12));assertEquals(25,new HashSet<>(card).size());assertEquals(30,new HashSet<>(calls).size());for(int i=0;i<25;i++)if(i!=12)assertTrue(card.get(i)>i%5*15&&card.get(i)<=i%5*15+15);for(int n:calls)assertTrue(n>=1&&n<=75);assertEquals(BingoEngine.winningLines(card,calls),r.slot());assertEquals(r.slot()==0?0:675,r.multiplierBps());}}
 @Test void allTwelvePatternsAndFreeCenter(){var card=new ArrayList<Integer>();for(int i=0;i<25;i++)card.add(i==12?0:i+1);var lines=BingoEngine.lines();assertEquals(12,lines.size());for(int i=0;i<12;i++){var calls=lines.get(i).stream().filter(c->c!=12).map(card::get).toList();assertEquals(1<<i,BingoEngine.winningLines(card,calls));assertEquals(0,BingoEngine.winningLines(card,calls.subList(0,calls.size()-1)));}assertEquals(4095,BingoEngine.winningLines(card,card));}
 @Test void multipleLinesPayOnceAndFractionsRoundDown(){assertEquals(3381,ArcadeEngine.payout(501,new ArcadeEngine.Outcome(List.of(),4095,675,"BINGO")));}
 @Test void exactInclusionExclusionConfirmsPaytable(){double probability=0;var lines=BingoEngine.lines();for(int mask=1;mask<4096;mask++){Set<Integer> union=new HashSet<>();for(int i=0;i<12;i++)if((mask&(1<<i))!=0)union.addAll(lines.get(i));union.remove(12);double chance=1;for(int i=0;i<union.size();i++)chance*=((double)(30-i))/(75-i);probability+=(Integer.bitCount(mask)%2==1?1:-1)*chance;}assertEquals(.1435394694879549,probability,1e-12);assertEquals(.9688914190436955,probability*6.75,1e-12);}
}
