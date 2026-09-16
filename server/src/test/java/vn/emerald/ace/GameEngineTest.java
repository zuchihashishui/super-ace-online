package vn.emerald.ace;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class GameEngineTest {
 final GameEngine engine=new GameEngine(new SplittableRandom(117)::nextInt);
 String[][] blank(){String[][] g=new String[5][4];for(var col:g)Arrays.fill(col,"S");return g;}
 @Test void wildMustParticipateInRealWinningWay(){var g=blank();g[0][0]="A";g[0][1]="W";g[1][0]="W";g[2][0]="W";var result=engine.evaluate(g);assertEquals(3400,result.units());assertEquals(Set.of("0,0","1,0","2,0"),result.hits());}
 @Test void allWildNoPayoutAndAllAce1024Ways(){var g=blank();for(var c:g)Arrays.fill(c,"W");assertEquals(0,engine.evaluate(g).units());for(var c:g)Arrays.fill(c,"A");assertEquals(3400L*4*1024,engine.evaluate(g).units());}
 @Test void scatterAwardAndRounding(){var g=blank();g[0][0]="A";g[1][0]="A";g[2][0]="A";var result=new GameEngine(n->123).spin(2000,g);assertEquals(10,result.freeAward());assertEquals(680,result.winCents());assertEquals(1,result.cascades().size());assertThrows(IllegalArgumentException.class,()->engine.spin(1));}
 @Test void distributionHas124Entries(){Map<String,Integer> counts=new HashMap<>();for(int i=0;i<124;i++){int chosen=i;String s=new GameEngine(n->chosen).symbol();counts.merge(s,1,Integer::sum);}assertEquals(Map.of("A",8,"K",10,"Q",12,"J",14,"10",16,"9",18,"8",20,"7",22,"W",1,"S",3),counts);}
 @Test void matchesIndependentExhaustiveOracle(){String[] symbols={"A","K","Q","J","10","9","8","7","W","W","S"};SplittableRandom r=new SplittableRandom(77);for(int i=0;i<2500;i++){var grid=blank();for(int c=0;c<5;c++)for(int row=0;row<4;row++)grid[c][row]=symbols[r.nextInt(symbols.length)];var actual=engine.evaluate(grid);var expected=oracle(grid);assertEquals(expected.units(),actual.units());assertEquals(expected.hits(),actual.hits());}}
 GameEngine.Evaluation oracle(String[][] grid){String[] symbols={"A","K","Q","J","10","9","8","7"};int[] pays={3400,2125,2125,1275,1275,850,680,510};long sum=0;Set<String> hits=new HashSet<>();for(int i=0;i<symbols.length;i++){String symbol=symbols[i];int cols=0;while(cols<5&&Arrays.stream(grid[cols]).anyMatch(s->s.equals(symbol)||s.equals("W")))cols++;if(cols>=3)sum+=enumerate(grid,symbol,cols,0,false,new ArrayList<>(),hits)*pays[i]*(1L<<(cols-3));}return new GameEngine.Evaluation(sum,hits);}
 long enumerate(String[][] g,String s,int cols,int c,boolean real,List<String> path,Set<String> hits){if(c==cols){if(real){hits.addAll(path);return 1;}return 0;}long n=0;for(int r=0;r<4;r++)if(g[c][r].equals(s)||g[c][r].equals("W")){path.add(c+","+r);n+=enumerate(g,s,cols,c+1,real||g[c][r].equals(s),path,hits);path.removeLast();}return n;}
}
