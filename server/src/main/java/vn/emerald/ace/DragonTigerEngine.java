package vn.emerald.ace;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.IntUnaryOperator;

/** Custom two-card, last-digit game. No third card and no history-based adjustments. */
public final class DragonTigerEngine {
 public enum Side { DRAGON, TIGER, TIE }
 public record Card(int rank,String suit) {
  public int points(){return rank>=10?0:rank;}
 }
 public record Outcome(List<Card> dragon,List<Card> tiger,int dragonScore,int tigerScore,Side winner){}
 private final IntUnaryOperator random;
 public DragonTigerEngine(){this(new SecureRandom()::nextInt);}
 DragonTigerEngine(IntUnaryOperator random){this.random=random;}
 public static int score(List<Card> cards){return cards.stream().mapToInt(Card::points).sum()%10;}
 public static Outcome compare(List<Card> dragon,List<Card> tiger){
  int d=score(dragon),t=score(tiger);
  return new Outcome(List.copyOf(dragon),List.copyOf(tiger),d,t,d==t?Side.TIE:d>t?Side.DRAGON:Side.TIGER);
 }
 public Outcome deal(){
  int[] deck=new int[52];for(int i=0;i<52;i++)deck[i]=i;
  var cards=new ArrayList<Card>();String[] suits={"SPADES","HEARTS","DIAMONDS","CLUBS"};
  for(int i=0;i<4;i++){int j=i+random.applyAsInt(52-i),tmp=deck[i];deck[i]=deck[j];deck[j]=tmp;cards.add(new Card(deck[i]%13+1,suits[deck[i]/13]));}
  return compare(List.of(cards.get(0),cards.get(2)),List.of(cards.get(1),cards.get(3)));
 }
 /** Gross return includes the stake. Round a half-cent up, once per round. */
 public static long payout(long bet,Side side,Side winner){
  if(!GameEngine.validBet(bet))throw new IllegalArgumentException("Invalid bet");
  if(side==winner)return side==Side.TIE?Math.multiplyExact(bet,9):(Math.multiplyExact(bet,195)+50)/100;
  return winner==Side.TIE&&side!=Side.TIE?bet:0;
 }
}
