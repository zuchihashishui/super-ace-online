package philip.emerald.ace.colorgame;
import philip.emerald.ace.superace.GameEngine;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** Three independent fair six-colour dice. Gross returns include the stake. */
public final class ColorGameEngine {
 public enum Side { YELLOW, WHITE, PINK, BLUE, RED, GREEN }
 public record Outcome(List<Side> dice){public Outcome{dice=List.copyOf(dice);if(dice.size()!=3)throw new IllegalArgumentException("Three dice required");}}
 private final IntUnaryOperator random;
 public ColorGameEngine(){this(new SecureRandom()::nextInt);}
 ColorGameEngine(IntUnaryOperator random){this.random=random;}
 public Outcome deal(){return new Outcome(List.of(Side.values()[random.applyAsInt(6)],Side.values()[random.applyAsInt(6)],Side.values()[random.applyAsInt(6)]));}
 public static long payout(long bet,Side side,Outcome outcome){if(!GameEngine.validBet(bet)||side==null)throw new IllegalArgumentException("Invalid bet");long matches=outcome.dice().stream().filter(c->c==side).count();return matches==0?0:Math.multiplyExact(bet,matches+1);}
}
