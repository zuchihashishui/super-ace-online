package philip.emerald.ace.luckywheel;
import philip.emerald.ace.Utils.ArcadeEngine;
import java.util.*;
import java.util.function.IntUnaryOperator;
public final class LuckyWheelEngine {
 private final IntUnaryOperator random;
 public LuckyWheelEngine(IntUnaryOperator random){this.random=random;}
 public static final List<Integer> PAYTABLE=List.of(0,100,0,50,0,200,0,50,0,100,0,300,0,50,0,700);
 public ArcadeEngine.Outcome play(){int slot=random.applyAsInt(PAYTABLE.size());return new ArcadeEngine.Outcome(List.of(slot),slot,PAYTABLE.get(slot),"WHEEL");}
}
