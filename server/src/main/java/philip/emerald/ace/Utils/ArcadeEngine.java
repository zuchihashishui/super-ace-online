package philip.emerald.ace.Utils;
import philip.emerald.ace.luckywheel.LuckyWheelEngine;
import philip.emerald.ace.slots.SlotsEngine;
import philip.emerald.ace.plinko.PlinkoEngine;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** Explicit independent virtual-chip game rules. Multipliers are in hundredths. */
public final class ArcadeEngine {
 public enum Game { PLINKO, WHEEL, SLOTS, SAKLA, BINGO }
 public record Outcome(List<Integer> values,int slot,int multiplierBps,String label){public Outcome{values=List.copyOf(values);}}
 private final IntUnaryOperator random;
 public ArcadeEngine(){this(new SecureRandom()::nextInt);}
 ArcadeEngine(IntUnaryOperator random){this.random=random;}
 public static final List<Integer> PLINKO=PlinkoEngine.PAYTABLE;
 public static final List<Integer> WHEEL=LuckyWheelEngine.PAYTABLE;
 public Outcome play(Game game){return play(game,0);}
 public Outcome play(Game game,int selection){return switch(game){case PLINKO->new PlinkoEngine(random).play();case WHEEL->new LuckyWheelEngine(random).play();case SLOTS->new SlotsEngine(random).play();case SAKLA->new philip.emerald.ace.sakla.SaklaEngine(random).play(selection);case BINGO->new philip.emerald.ace.bingo.BingoEngine(random).play();};}
 public static long payout(long bet,Outcome outcome){return Math.multiplyExact(bet,outcome.multiplierBps())/100;}
}
