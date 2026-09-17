package philip.emerald.ace.plinko;
import philip.emerald.ace.Utils.ArcadeEngine;
import java.util.*;
import java.util.function.IntUnaryOperator;
public final class PlinkoEngine {
 private final IntUnaryOperator random;
 public PlinkoEngine(IntUnaryOperator random){this.random=random;}
 public static final List<Integer> PAYTABLE=List.of(5000,1000,300,150,100,70,50,70,100,150,300,1000,5000);
 public ArcadeEngine.Outcome play(){var route=new ArrayList<Integer>();int slot=0;for(int i=0;i<12;i++){int direction=random.applyAsInt(2);route.add(direction);slot+=direction;}return new ArcadeEngine.Outcome(route,slot,PAYTABLE.get(slot),"PLINKO");}
}
