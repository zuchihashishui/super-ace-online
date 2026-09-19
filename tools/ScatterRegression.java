import philip.emerald.ace.superace.GameEngine;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
public class ScatterRegression {
 static String[][] board(String top){return new String[][]{{top,"K","K","K"},{top,"Q","Q","Q"},{top,"J","J","J"},{"10","10","10","10"},{"9","9","9","9"}};}
 static void check(boolean condition,String name){if(!condition)throw new AssertionError(name);System.out.println("PASS "+name);}
 public static void main(String[] args){
  var engine=new GameEngine(n->123);
  check(engine.spin(500,board("S")).freeAward()==10,"initial 3 Scatter awards 10");
  var zero=board("S");zero[2][0]="J";
  check(engine.spin(500,zero).freeAward()==0,"2 Scatter gives no award");
  var after=engine.spin(500,board("A"));
  check(after.cascades().size()==1&&after.freeAward()==10,"third Scatter from first cascade awards 10");
  check(Arrays.stream(after.finalBoard()).flatMap(Arrays::stream).filter("S"::equals).count()==3,"final board matches bonus");
  var rng=new AtomicInteger();
  var later=new GameEngine(n->rng.getAndIncrement()<3?0:123).spin(500,board("A"));
  check(later.cascades().size()==2&&later.freeAward()==10,"bonus after second cascade awarded once");
  var both=board("A");for(int i=0;i<3;i++)both[i][1]="S";
  rng.set(0);var repeated=new GameEngine(n->rng.getAndIncrement()<3?0:123).spin(500,both);
  check(repeated.cascades().size()==2&&repeated.freeAward()==10,"initial plus multiple cascade Scatter never duplicates");
  var four=board("S");four[3][0]="S";
  check(engine.spin(500,four).freeAward()==10,"4 Scatter still awards exactly 10");
  for(String mode:List.of("RTP_10000","RTP_9750","LEGACY")){
   check(new GameEngine(n->123,mode).spin(500,board("A")).freeAward()==10,"same bonus in "+mode);
  }
 }
}
