package vn.emerald.ace;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class DragonTigerEngineTest{
 DragonTigerEngine.Card card(int r){return new DragonTigerEngine.Card(r,"SPADES");}
 @Test void scoringAndAllOutcomes(){
  assertEquals(5,DragonTigerEngine.score(List.of(card(8),card(7))));
  assertEquals(0,DragonTigerEngine.score(List.of(card(10),card(13))));
  assertEquals(2,DragonTigerEngine.score(List.of(card(1),card(1))));
  assertEquals(DragonTigerEngine.Side.DRAGON,DragonTigerEngine.compare(List.of(card(8),card(7)),List.of(card(1),card(3))).winner());
  assertEquals(DragonTigerEngine.Side.TIGER,DragonTigerEngine.compare(List.of(card(10),card(12)),List.of(card(8),card(1))).winner());
  assertEquals(DragonTigerEngine.Side.TIE,DragonTigerEngine.compare(List.of(card(9),card(1)),List.of(card(10),card(12))).winner());
 }
 @Test void payoutMatrixAndCentRounding(){
  for(var choice:DragonTigerEngine.Side.values())for(var winner:DragonTigerEngine.Side.values()){
   long expected=choice==winner?(choice==DragonTigerEngine.Side.TIE?4500:975):winner==DragonTigerEngine.Side.TIE?500:0;
   assertEquals(expected,DragonTigerEngine.payout(500,choice,winner));
  }
  assertEquals(1472,DragonTigerEngine.payout(755,DragonTigerEngine.Side.DRAGON,DragonTigerEngine.Side.DRAGON));
  assertThrows(IllegalArgumentException.class,()->DragonTigerEngine.payout(499,DragonTigerEngine.Side.TIE,DragonTigerEngine.Side.TIE));
 }
 @Test void deckHasFourDistinctCardsAndBothSidesAreReachable(){
  var random=new Random(7638);var engine=new DragonTigerEngine(random::nextInt);var seen=EnumSet.noneOf(DragonTigerEngine.Side.class);
  for(int i=0;i<10000;i++){var o=engine.deal();var cards=new HashSet<>(o.dragon());cards.addAll(o.tiger());assertEquals(4,cards.size());assertTrue(o.dragonScore()>=0&&o.dragonScore()<=9);assertTrue(o.tigerScore()>=0&&o.tigerScore()<=9);seen.add(o.winner());}
  assertEquals(3,seen.size());
 }
}
