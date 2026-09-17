package philip.emerald.ace.sakla;
import philip.emerald.ace.sakla.SaklaEngine;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class SaklaEngineTest {
 @Test void fixedPairsCoverExactlyTwoCardsEach(){int[] counts=new int[20];for(int i=0;i<40;i++)counts[SaklaEngine.pair(i)]++;for(int n:counts)assertEquals(2,n);assertEquals(SaklaEngine.pair(0),SaklaEngine.pair(9));assertEquals(SaklaEngine.pair(7),SaklaEngine.pair(8));}
 @Test void stopsAtFirstCompletePairAndPaysSelectedPair(){for(int seed=0;seed<2000;seed++){var rng=new Random(seed);var r=new SaklaEngine(rng::nextInt).play(seed%20);assertEquals(r.values().size(),new HashSet<>(r.values()).size());assertTrue(r.values().size()>=2&&r.values().size()<=21);Set<Integer> seen=new HashSet<>();for(int i=0;i<r.values().size()-1;i++)assertTrue(seen.add(SaklaEngine.pair(r.values().get(i))));assertTrue(seen.contains(SaklaEngine.pair(r.values().getLast())));assertEquals(SaklaEngine.pair(r.values().getLast()),r.slot());assertEquals(r.slot()==seed%20?1900:0,r.multiplierBps());}}
 @Test void rejectsInvalidSelection(){assertThrows(IllegalArgumentException.class,()->new SaklaEngine().play(20));assertThrows(IllegalArgumentException.class,()->new SaklaEngine().play(-1));}
}
