package philip.emerald.ace.slots;
import philip.emerald.ace.Utils.ArcadeEngine;
import java.util.*;
import java.util.function.IntUnaryOperator;
public final class SlotsEngine {
 private final IntUnaryOperator random;
 public SlotsEngine(IntUnaryOperator random){this.random=random;}
 public ArcadeEngine.Outcome play(){var symbols=new ArrayList<Integer>();for(int i=0;i<3;i++){int v=random.applyAsInt(10);symbols.add(v<4?0:v<7?1:v<9?2:3);}int a=symbols.get(0),sevens=(int)symbols.stream().filter(v->v==3).count();int multiplier=symbols.stream().allMatch(v->v==a)?List.of(500,1000,2500,10000).get(a):sevens==2?300:0;return new ArcadeEngine.Outcome(symbols,0,multiplier,"SLOTS");}
}
