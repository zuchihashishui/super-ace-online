package philip.emerald.ace.mines;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class MinesEngineTest {
 @Test void everyMultiplierRespectsProbabilityAndRounding(){for(int count:MinesEngine.COUNTS){long previous=100;for(int k=1;k<=25-count;k++){long multiplier=MinesEngine.multiplier(count,k),numerator=97*MinesEngine.choose(25,k),denominator=MinesEngine.choose(25-count,k);assertEquals(numerator/denominator,multiplier);assertTrue(multiplier>=previous);assertTrue(multiplier*denominator<=numerator);assertTrue((multiplier+1)*denominator>numerator);previous=multiplier;}assertEquals(97*MinesEngine.choose(25,count),previous);}}
 @Test void boardsContainExactlyDistinctValidMines(){var engine=new MinesEngine();for(int count:MinesEngine.COUNTS)for(int n=0;n<1000;n++){var board=engine.board(count);assertEquals(count,new HashSet<>(board).size());assertTrue(board.stream().allMatch(v->v>=0&&v<25));}assertEquals(550,MinesEngine.payout(500,3,1));assertThrows(IllegalArgumentException.class,()->engine.board(2));}
}
