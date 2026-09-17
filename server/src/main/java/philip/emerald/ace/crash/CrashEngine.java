package philip.emerald.ace.crash;

import java.security.SecureRandom;
/** Integer hundredths of a multiplier; server time is authoritative. */
public final class CrashEngine {
 public static final long BETTING_MS=10000,REVEAL_MS=5000;
 public static final int MAX_BPS=10000;
 private final SecureRandom random=new SecureRandom();
 public int sample(){return fromDraw(random.nextInt(1000000)+1);}
 static int fromDraw(int draw){if(draw<1||draw>1000000)throw new IllegalArgumentException();return Math.max(100,Math.min(MAX_BPS,97000000/draw));}
 public static long at(long flightAt,int multiplier){return flightAt+(long)Math.ceil(Math.log(multiplier/100.0)*8000);}
 public static int multiplier(long flightAt,long now){return Math.min(MAX_BPS,(int)Math.floor(100*Math.exp(Math.max(0,now-flightAt)/8000.0)));}
 public static long payout(long stake,int multiplier){return Math.multiplyExact(stake,multiplier)/100;}
 public static boolean validAuto(int value){return value==0||value>=101&&value<MAX_BPS;}
}
