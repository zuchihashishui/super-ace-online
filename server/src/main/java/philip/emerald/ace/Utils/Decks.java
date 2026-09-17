package philip.emerald.ace.Utils;
import java.util.*;
import java.util.function.IntUnaryOperator;
/** Fisher-Yates sampling without replacement; secure randomness supplied by each game. */
public final class Decks {
 private Decks(){}
 public static List<Integer> shuffle(int size,IntUnaryOperator random){var deck=new ArrayList<Integer>(size);for(int i=0;i<size;i++)deck.add(i);for(int i=size-1;i>0;i--)Collections.swap(deck,i,random.applyAsInt(i+1));return List.copyOf(deck);}
}
